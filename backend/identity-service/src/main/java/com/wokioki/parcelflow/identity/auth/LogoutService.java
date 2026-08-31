package com.wokioki.parcelflow.identity.auth;

import com.wokioki.parcelflow.identity.auth.dto.LogoutRequest;
import com.wokioki.parcelflow.identity.auth.refreshtoken.RefreshTokenService;
import org.springframework.stereotype.Service;

@Service
public class LogoutService {

    private final RefreshTokenService refreshTokenService;

    public LogoutService(
        RefreshTokenService refreshTokenService
    ) {
        this.refreshTokenService = refreshTokenService;
    }

    public void logout(LogoutRequest request) {
        refreshTokenService.revoke(
            request.refreshToken()
        );
    }
}
