package com.wokioki.parcelflow.identity.auth;

import com.wokioki.parcelflow.identity.auth.dto.LoginRequest;
import com.wokioki.parcelflow.identity.auth.dto.LoginResponse;
import com.wokioki.parcelflow.identity.security.jwt.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public LoginService(
        AuthenticationManager authenticationManager,
        TokenService tokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.email()
            .trim()
            .toLowerCase();

        Authentication authenticationRequest =
            UsernamePasswordAuthenticationToken.unauthenticated(
                normalizedEmail,
                request.password()
            );

        Authentication authentication =
            authenticationManager.authenticate(authenticationRequest);

        String accessToken =
            tokenService.generateAccessToken(authentication);

        return new LoginResponse(
            accessToken,
            "Bearer"
        );
    }
}
