package com.wokioki.parcelflow.identity.auth;

import com.wokioki.parcelflow.identity.auth.dto.LoginRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;

    public LoginService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public Authentication authenticate(LoginRequest request) {
        String normalizedEmail = request.email()
            .trim()
            .toLowerCase();

        Authentication authenticationRequest =
            UsernamePasswordAuthenticationToken.unauthenticated(
                normalizedEmail,
                request.password()
            );

        return authenticationManager.authenticate(authenticationRequest);
    }
}
