package com.javacore.spring_api_luvine.product.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateVariantRequest(
        @NotBlank(message = "Informe a cor")
        @Size(min = 2, max = 50, message = "A cor deve ter entre 3 a 50 caracteres")
        @Pattern(regexp = "^[\\p{L} ]+$", message = "Cor inválida")
        String color,

        @NotBlank(message = "Informe o tamanho")
        @Size(max = 20, message = "O tamanho deve ter no máximo 20 caracteres")
        @Pattern(regexp = "^[A-Z0-9 ]+$", message = "Tamanho inválido")
        String size,

        @NotNull(message = "Informe o preço")
        @Positive(message = "O preço deve ser maior que zero")
        BigDecimal price,

        @NotNull(message = "Informe a quantidade em estoque")
        @PositiveOrZero(message = "A quantidade em estoque não pode ser negativa")
        Integer stockQuantity
) {
}