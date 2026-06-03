package com.javacore.spring_api_luvine.product.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateVariantStockRequest(
        @NotNull(message = "Informe a quantidade em estoque")
        @PositiveOrZero(message = "A quantidade em estoque não pode ser negativa")
        Integer stockQuantity
) {
}