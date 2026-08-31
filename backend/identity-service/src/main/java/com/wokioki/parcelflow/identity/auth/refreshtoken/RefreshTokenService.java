package com.wokioki.parcelflow.identity.auth.refreshtoken;

import com.wokioki.parcelflow.identity.security.jwt.JwtProperties;
import com.wokioki.parcelflow.identity.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(
        RefreshTokenRepository refreshTokenRepository,
        RefreshTokenGenerator refreshTokenGenerator,
        JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public String createRefreshToken(User user) {
        String rawToken =
            refreshTokenGenerator.generateToken();

        String tokenHash =
            refreshTokenGenerator.hashToken(rawToken);

        Instant expiresAt = Instant.now()
            .plus(jwtProperties.refreshTokenTtl());

        RefreshToken refreshToken = new RefreshToken(
            user,
            tokenHash,
            expiresAt
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public void revoke(String rawToken) {
        String tokenHash =
            refreshTokenGenerator.hashToken(rawToken);

        RefreshToken refreshToken =
            refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(
                    InvalidRefreshTokenException::new
                );

        if (!refreshToken.isActive()) {
            throw new InvalidRefreshTokenException();
        }

        refreshToken.revoke();
    }

    @Transactional
    public RefreshTokenRotation rotate(String rawToken) {
        String tokenHash =
            refreshTokenGenerator.hashToken(rawToken);

        RefreshToken currentToken =
            refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(
                    InvalidRefreshTokenException::new
                );

        if (!currentToken.isActive()) {
            throw new InvalidRefreshTokenException();
        }

        User user = currentToken.getUser();

        String newRawToken =
            refreshTokenGenerator.generateToken();

        String newTokenHash =
            refreshTokenGenerator.hashToken(newRawToken);

        RefreshToken replacement = new RefreshToken(
            user,
            newTokenHash,
            Instant.now().plus(
                jwtProperties.refreshTokenTtl()
            )
        );

        refreshTokenRepository.save(replacement);

        currentToken.replaceWith(replacement);

        return new RefreshTokenRotation(
            user,
            newRawToken
        );
    }

    public record RefreshTokenRotation(
        User user,
        String refreshToken
    ) {
    }
}
