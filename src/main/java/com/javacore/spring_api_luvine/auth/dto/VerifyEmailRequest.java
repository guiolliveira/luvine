package com.javacore.spring_api_luvine.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailRequest(

        @NotBlank(message = "Informe seu email")
        @Email(message = "Email é inválido")
        String email,

        @NotBlank(message = "Informe o código de verificação")
        @Pattern(regexp = "^\\d{6}$", message = "O código de verificação deve conter exatamente 6 digitos")
        String code
) {
}