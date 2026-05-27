package com.javacore.spring_api_luvine.product.application.dto;

import com.javacore.spring_api_luvine.product.domain.entity.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(

        @Size(max = 150, message = "A categoria deve ter no máximo 150 caracteres")
        String newCategorySlug,

        @Size(min = 3, message = "O nome do produto deve ter no minimo 3 caracteres")
        @Pattern(regexp = "^[\\p{L}\\d\\s\\-.,()]+$", message = "Nome do produto inválido")
        String newProductName,

        @Size(min = 3, message = "A descrição do produto deve ter no minimo 3 caracteres")
        String newDescription,

        @Positive(message = "Preço base tem que ser maior que zero")
        BigDecimal newBasePrice,

        @NotNull(message = "Informe o status do produto")
        Status newStatus
) {
}