package com.javacore.spring_api_luvine.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 100, message = "Nome deve conter entre 2 a 100 caracteres")
        @Pattern(regexp = "^[\\p{L} ]+$", message = "Nome não deve conter números ou caracteres especiais")
        String newFirstName,

        @Size(min = 2, max = 100, message = "Sobrenome deve conter entre 2 a 100 caracteres")
        @Pattern(regexp = "^[\\p{L} ]+$", message = "Sobre nome não deve conter números ou caracteres especiais")
        String newLastName
) {
}