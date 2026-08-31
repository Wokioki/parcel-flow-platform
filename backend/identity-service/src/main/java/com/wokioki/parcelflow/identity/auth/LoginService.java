package com.wokioki.parcelflow.identity.auth;

import com.wokioki.parcelflow.identity.auth.dto.LoginRequest;
import com.wokioki.parcelflow.identity.auth.dto.LoginResponse;
import com.wokioki.parcelflow.identity.auth.refreshtoken.RefreshTokenService;
import com.wokioki.parcelflow.identity.security.jwt.TokenService;
import com.wokioki.parcelflow.identity.user.User;
import com.wokioki.parcelflow.identity.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public LoginService(
        AuthenticationManager authenticationManager,
        TokenService tokenService,
        RefreshTokenService refreshTokenService,
        UserRepository userRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
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

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "Authenticated user not found"
                )
            );

        String accessToken =
            tokenService.generateAccessToken(user);

        String refreshToken =
            refreshTokenService.createRefreshToken(user);

        return new LoginResponse(
            accessToken,
            refreshToken,
            "Bearer"
        );
    }
}
