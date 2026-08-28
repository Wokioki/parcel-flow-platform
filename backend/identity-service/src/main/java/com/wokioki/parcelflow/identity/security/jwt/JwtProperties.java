package com.wokioki.parcelflow.identity.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
    String issuer,
    Duration accessTokenTtl,
    Duration refreshTokenTtl
) {
}
