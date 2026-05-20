package com.javacore.spring_api_luvine.user.application.dto;

import com.javacore.spring_api_luvine.user.domain.entity.UserRole;

import java.util.UUID;

public record ProfileResponse(
        UUID publicId,
        String email,
        String firstName,
        String lastName,
        UserRole userRole,
        String avatarInitial
) {
}