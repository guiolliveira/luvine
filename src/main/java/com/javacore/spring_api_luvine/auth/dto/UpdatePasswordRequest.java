package com.javacore.spring_api_luvine.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(
        @NotBlank(message = "Informe a sua nova senha")
        @Size(min = 8, message = "A nova senha deve conter ao menos 8 caracteres")
        @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$",
                message = "A nova senha é inválida")
        String newPassword,

        @NotBlank(message = "É necessário confirmar a nova senha")
        String confirmPassword
) {
}
