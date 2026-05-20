package com.javacore.spring_api_luvine.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Informe o seu email")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email é inválido")
        String email,

        @NotBlank(message = "Informe o seu nome")
        @Size(min = 2, max = 100, message = "Nome deve conter entre 2 a 100 caracteres")
        @Pattern(regexp = "^[\\p{L} ]+$", message = "Nome não deve conter números ou caracteres especiais")
        String firstName,

        @NotBlank(message = "Informe o seu sobrenome")
        @Size(min = 2, max = 100, message = "Sobrenome deve conter entre 2 a 100 caracteres")
        @Pattern(regexp = "^[\\p{L} ]+$", message = "Sobre nome não deve conter números ou caracteres especiais")
        String lastName,

        @NotBlank(message = "Informe a sua senha")
        @Size(min = 8, message = "Senha deve conter ao menos 8 caracteres")
        @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$",
                message = "Senha é inválida")
        String password,

        @NotBlank(message = "É necessário confirmar sua senha")
        String confirmPassword
) {
}