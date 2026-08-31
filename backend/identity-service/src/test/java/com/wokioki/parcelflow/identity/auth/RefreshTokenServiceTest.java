package com.wokioki.parcelflow.identity.auth;

import com.wokioki.parcelflow.identity.auth.refreshtoken.InvalidRefreshTokenException;
import com.wokioki.parcelflow.identity.auth.refreshtoken.RefreshToken;
import com.wokioki.parcelflow.identity.auth.refreshtoken.RefreshTokenGenerator;
import com.wokioki.parcelflow.identity.auth.refreshtoken.RefreshTokenRepository;
import com.wokioki.parcelflow.identity.auth.refreshtoken.RefreshTokenService;
import com.wokioki.parcelflow.identity.security.jwt.JwtProperties;
import com.wokioki.parcelflow.identity.user.Role;
import com.wokioki.parcelflow.identity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;

    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
            "parcel-flow-identity",
            Duration.ofMinutes(15),
            Duration.ofDays(7)
        );

        refreshTokenService = new RefreshTokenService(
            refreshTokenRepository,
            refreshTokenGenerator,
            jwtProperties
        );

        user = new User(
            "john@example.com",
            "password-hash",
            "John",
            "Doe",
            Role.CUSTOMER
        );
    }

    @Test
    void shouldCreateRefreshToken() {
        when(refreshTokenGenerator.generateToken())
            .thenReturn("raw-token");

        when(refreshTokenGenerator.hashToken("raw-token"))
            .thenReturn("hashed-token");

        String result =
            refreshTokenService.createRefreshToken(user);

        assertEquals("raw-token", result);

        verify(refreshTokenRepository)
            .save(any(RefreshToken.class));
    }

    @Test
    void shouldRotateRefreshToken() {
        RefreshToken currentToken = new RefreshToken(
            user,
            "old-hash",
            Instant.now().plus(Duration.ofDays(1))
        );

        when(refreshTokenGenerator.hashToken("old-raw-token"))
            .thenReturn("old-hash");

        when(refreshTokenRepository.findByTokenHashWithUser("old-hash"))
            .thenReturn(Optional.of(currentToken));

        when(refreshTokenGenerator.generateToken())
            .thenReturn("new-raw-token");

        when(refreshTokenGenerator.hashToken("new-raw-token"))
            .thenReturn("new-hash");

        RefreshTokenService.RefreshTokenRotation result =
            refreshTokenService.rotate("old-raw-token");

        assertEquals(user, result.user());
        assertEquals(
            "new-raw-token",
            result.refreshToken()
        );

        assertTrue(currentToken.isRevoked());
        assertNotNull(
            currentToken.getReplacedByToken()
        );

        verify(refreshTokenRepository)
            .save(any(RefreshToken.class));
    }

    @Test
    void shouldRejectUnknownRefreshToken() {
        when(refreshTokenGenerator.hashToken("unknown-token"))
            .thenReturn("unknown-hash");

        when(refreshTokenRepository.findByTokenHashWithUser("unknown-hash"))
            .thenReturn(Optional.empty());

        assertThrows(
            InvalidRefreshTokenException.class,
            () -> refreshTokenService.rotate(
                "unknown-token"
            )
        );
    }

    @Test
    void shouldRejectRevokedRefreshToken() {
        RefreshToken revokedToken = new RefreshToken(
            user,
            "revoked-hash",
            Instant.now().plus(Duration.ofDays(1))
        );

        revokedToken.revoke();

        when(refreshTokenGenerator.hashToken("revoked-token"))
            .thenReturn("revoked-hash");

        when(refreshTokenRepository.findByTokenHashWithUser("revoked-hash"))
            .thenReturn(Optional.of(revokedToken));

        assertThrows(
            InvalidRefreshTokenException.class,
            () -> refreshTokenService.rotate(
                "revoked-token"
            )
        );
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        RefreshToken expiredToken = new RefreshToken(
            user,
            "expired-hash",
            Instant.now().minus(Duration.ofMinutes(1))
        );

        when(refreshTokenGenerator.hashToken("expired-token"))
            .thenReturn("expired-hash");

        when(refreshTokenRepository.findByTokenHashWithUser("expired-hash"))
            .thenReturn(Optional.of(expiredToken));

        assertThrows(
            InvalidRefreshTokenException.class,
            () -> refreshTokenService.rotate(
                "expired-token"
            )
        );
    }

    @Test
    void shouldRevokeActiveRefreshToken() {
        RefreshToken refreshToken = new RefreshToken(
            user,
            "token-hash",
            Instant.now().plus(Duration.ofDays(1))
        );

        when(refreshTokenGenerator.hashToken("raw-token"))
            .thenReturn("token-hash");

        when(refreshTokenRepository.findByTokenHash("token-hash"))
            .thenReturn(Optional.of(refreshToken));

        refreshTokenService.revoke("raw-token");

        assertTrue(refreshToken.isRevoked());
    }
}
