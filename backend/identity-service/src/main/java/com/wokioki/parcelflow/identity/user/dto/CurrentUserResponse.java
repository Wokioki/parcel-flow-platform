package com.wokioki.parcelflow.identity.user.dto;

import com.wokioki.parcelflow.identity.user.Role;

import java.util.UUID;

public record CurrentUserResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    Role role
) {
}
