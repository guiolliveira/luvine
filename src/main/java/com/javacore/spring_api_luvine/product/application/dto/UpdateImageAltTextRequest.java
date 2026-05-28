package com.javacore.spring_api_luvine.product.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateImageAltTextRequest(
        @NotBlank(message = "Informe o texto alternativo")
        @Size(max = 150, message = "O texto alternativo deve ter no máximo 150 caracteres")
        String newAltText
) {
}