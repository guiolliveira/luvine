package com.javacore.spring_api_luvine.auth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Informe o refresh token")
        String refreshToken
) {
}
