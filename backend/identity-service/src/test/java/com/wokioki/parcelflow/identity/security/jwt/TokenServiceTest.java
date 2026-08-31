package com.wokioki.parcelflow.identity.security.jwt;

import com.wokioki.parcelflow.identity.user.Role;
import com.wokioki.parcelflow.identity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TokenServiceTest {

    private JwtEncoder jwtEncoder;
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        jwtEncoder = mock(JwtEncoder.class);

        JwtProperties jwtProperties = new JwtProperties(
            "parcel-flow-identity",
            Duration.ofMinutes(15),
            Duration.ofDays(7)
        );

        tokenService = new TokenService(
            jwtEncoder,
            jwtProperties
        );
    }

    @Test
    void shouldGenerateAccessToken() {
        User user = new User(
            "john@example.com",
            "password-hash",
            "John",
            "Doe",
            Role.CUSTOMER
        );

        UUID userId = UUID.randomUUID();

        ReflectionTestUtils.setField(
            user,
            "id",
            userId
        );

        Jwt jwt = new Jwt(
            "encoded-access-token",
            Instant.now(),
            Instant.now().plus(Duration.ofMinutes(15)),
            Map.of("alg", "RS256"),
            Map.of(
                "sub", userId.toString(),
                "email", "john@example.com",
                "roles", "ROLE_CUSTOMER"
            )
        );

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
            .thenReturn(jwt);

        String token = tokenService.generateAccessToken(user);

        assertEquals("encoded-access-token", token);
    }

    @Test
    void shouldGenerateAccessTokenWithExpectedClaims() {
        User user = new User(
            "john@example.com",
            "password-hash",
            "John",
            "Doe",
            Role.CUSTOMER
        );

        UUID userId = UUID.randomUUID();

        ReflectionTestUtils.setField(
            user,
            "id",
            userId
        );

        Jwt jwt = new Jwt(
            "encoded-access-token",
            Instant.now(),
            Instant.now().plus(Duration.ofMinutes(15)),
            Map.of("alg", "RS256"),
            Map.of("sub", userId.toString())
        );

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
            .thenReturn(jwt);

        tokenService.generateAccessToken(user);

        ArgumentCaptor<JwtEncoderParameters> captor =
            ArgumentCaptor.forClass(JwtEncoderParameters.class);

        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();

        assertEquals(
            userId.toString(),
            claims.getSubject()
        );

        assertEquals(
            "parcel-flow-identity",
            claims.getClaim("iss")
        );

        assertEquals(
            "john@example.com",
            claims.getClaim("email")
        );

        assertEquals(
            "ROLE_CUSTOMER",
            claims.getClaim("roles")
        );

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiresAt());
    }
}
