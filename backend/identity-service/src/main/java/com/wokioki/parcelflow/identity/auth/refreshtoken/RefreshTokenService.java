package com.wokioki.parcelflow.identity.auth.refreshtoken;

import com.wokioki.parcelflow.identity.security.jwt.JwtProperties;
import com.wokioki.parcelflow.identity.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshTokenService {

    private static final Logger log =
        LoggerFactory.getLogger(RefreshTokenService.class);

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

        log.info(
            "Created refresh token. hash={}, expiresAt={}",
            tokenHash,
            expiresAt
        );

        return rawToken;
    }

    @Transactional
    public void revoke(String rawToken) {
        String tokenHash =
            refreshTokenGenerator.hashToken(rawToken);

        log.info(
            "Revoke requested for token hash: {}",
            tokenHash
        );

        RefreshToken refreshToken =
            refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn(
                        "Refresh token not found during revoke: {}",
                        tokenHash
                    );

                    return new InvalidRefreshTokenException();
                });

        log.info(
            "Refresh token found for revoke. revokedAt={}, expiresAt={}, active={}",
            refreshToken.getRevokedAt(),
            refreshToken.getExpiresAt(),
            refreshToken.isActive()
        );

        if (!refreshToken.isActive()) {
            log.warn(
                "Refresh token rejected during revoke because it is inactive: {}",
                tokenHash
            );

            throw new InvalidRefreshTokenException();
        }

        refreshToken.revoke();

        log.info(
            "Refresh token revoked successfully: {}",
            tokenHash
        );
    }

    @Transactional
    public RefreshTokenRotation rotate(String rawToken) {
        String tokenHash =
            refreshTokenGenerator.hashToken(rawToken);

        log.info(
            "Refresh requested for token hash: {}",
            tokenHash
        );

        RefreshToken currentToken =
            refreshTokenRepository.findByTokenHashWithUser(tokenHash)
                .orElseThrow(() -> {
                    log.warn(
                        "Refresh token not found: {}",
                        tokenHash
                    );

                    return new InvalidRefreshTokenException();
                });

        log.info(
            "Refresh token found. revokedAt={}, expiresAt={}, active={}",
            currentToken.getRevokedAt(),
            currentToken.getExpiresAt(),
            currentToken.isActive()
        );

        if (!currentToken.isActive()) {
            log.warn(
                "Refresh token rejected because it is inactive: {}",
                tokenHash
            );

            throw new InvalidRefreshTokenException();
        }

        User user = currentToken.getUser();

        String newRawToken =
            refreshTokenGenerator.generateToken();

        String newTokenHash =
            refreshTokenGenerator.hashToken(newRawToken);

        Instant newExpiresAt = Instant.now()
            .plus(jwtProperties.refreshTokenTtl());

        RefreshToken replacement = new RefreshToken(
            user,
            newTokenHash,
            newExpiresAt
        );

        refreshTokenRepository.save(replacement);

        currentToken.replaceWith(replacement);

        log.info(
            "Refresh token rotated successfully. oldHash={}, newHash={}, newExpiresAt={}",
            tokenHash,
            newTokenHash,
            newExpiresAt
        );

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
