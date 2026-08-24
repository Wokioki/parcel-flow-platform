package com.wokioki.parcelflow.identity.auth.dto;

public record LoginResponse(
    String accessToken,
    String tokenType
) {
}
