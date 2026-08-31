package com.wokioki.parcelflow.identity.security.jwt;

import com.wokioki.parcelflow.identity.user.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public TokenService(
        JwtEncoder jwtEncoder,
        JwtProperties jwtProperties
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(
            jwtProperties.accessTokenTtl()
        );

        String role = "ROLE_" + user.getRole().name();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer())
            .subject(user.getId().toString())
            .issuedAt(now)
            .expiresAt(expiresAt)
            .claim("email", user.getEmail())
            .claim("roles", role)
            .build();

        return jwtEncoder.encode(
            JwtEncoderParameters.from(claims)
        ).getTokenValue();
    }
}
