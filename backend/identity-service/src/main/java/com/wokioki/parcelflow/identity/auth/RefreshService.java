package com.wokioki.parcelflow.identity.auth;

import com.wokioki.parcelflow.identity.auth.dto.LoginResponse;
import com.wokioki.parcelflow.identity.auth.dto.RefreshRequest;
import com.wokioki.parcelflow.identity.auth.refreshtoken.RefreshTokenService;
import com.wokioki.parcelflow.identity.security.jwt.TokenService;
import org.springframework.stereotype.Service;

@Service
public class RefreshService {

    private final RefreshTokenService refreshTokenService;
    private final TokenService tokenService;

    public RefreshService(
        RefreshTokenService refreshTokenService,
        TokenService tokenService
    ) {
        this.refreshTokenService = refreshTokenService;
        this.tokenService = tokenService;
    }

    public LoginResponse refresh(RefreshRequest request) {
        RefreshTokenService.RefreshTokenRotation rotation =
            refreshTokenService.rotate(
                request.refreshToken()
            );

        String accessToken =
            tokenService.generateAccessToken(
                rotation.user()
            );

        return new LoginResponse(
            accessToken,
            rotation.refreshToken(),
            "Bearer"
        );
    }
}
