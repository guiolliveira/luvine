package com.javacore.spring_api_luvine.product.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateVariantRequest(

        @Size(min = 2, max = 50, message = "A cor deve ter entre 3 a 50 caracteres")
        @Pattern(regexp = "^[\\p{L} ]+$", message = "Cor inválida")
        String newColor,

        @Size(max = 20, message = "O tamanho deve ter no máximo 20 caracteres")
        @Pattern(regexp = "^[A-Z0-9 ]+$", message = "Tamanho inválido")
        String newSize,

        @Positive(message = "O preço deve ser maior que zero")
        BigDecimal newPrice
) {
}