package com.javacore.spring_api_luvine.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendEmailRequest(
        @NotBlank(message = "Informe seu email")
        @Email(message = "Email é inválido")
        String email
) {
}