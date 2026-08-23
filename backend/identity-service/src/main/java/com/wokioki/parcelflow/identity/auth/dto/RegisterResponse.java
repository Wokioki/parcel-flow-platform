package com.wokioki.parcelflow.identity.auth.dto;

import com.wokioki.parcelflow.identity.user.Role;

import java.util.UUID;

public record RegisterResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    Role role
) {
}
