package com.javacore.spring_api_luvine.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;

import java.time.Instant;
import java.util.UUID;

public record RegisterResponse(
        UUID publicId,
        String email,
        String firstName,
        String lastName,

        @JsonFormat(pattern = "dd/MM/yyy HH:mm:ss", timezone = "America/Sao_Paulo")
        Instant createdAt,

        boolean active,

        UserProvider userProvider
) {
}