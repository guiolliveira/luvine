package com.javacore.spring_api_luvine.product.application.dto;

import com.javacore.spring_api_luvine.product.domain.entity.Status;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SearchProductRequest(
        @Size(max = 150, message = "O filtro de categoria deve ter no máximo 150 caracteres")
        String categorySlug,

        @Size(max = 150, message = "O termo de busca do produto deve ter no máximo 150 caracteres")
        String productName,

        @Size(max = 50, message = "O filtro de cor deve ter no máximo 50 caracteres")
        String color,

        @Size(max = 20, message = "O filtro de tamanho deve ter no máximo 20 caracteres")
        String size,

        @PositiveOrZero(message = "O preço mínimo não pode ser um valor negativo")
        BigDecimal minPrice,

        @PositiveOrZero(message = "O preço máximo não pode ser um valor negativo")
        BigDecimal maxPrice,

        Status status
) {
}