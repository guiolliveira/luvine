package com.javacore.spring_api_luvine.auth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Informe seu email")
        String email,

        @NotBlank(message = "Informe sua senha")
        String password
) {
}