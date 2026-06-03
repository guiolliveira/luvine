package com.javacore.spring_api_luvine.product.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductRequest(

        @NotNull(message = "Informe a categoria")
        @Size(max = 150, message = "A categoria deve ter no máximo 150 caracteres")
        String categorySlug,

        @NotNull(message = "Informe o nome do produto")
        @Size(min = 3, message = "O nome do produto deve ter no minimo 3 caracteres")
        @Pattern(regexp = "^[\\p{L}\\d\\s\\-.,()]+$", message = "Nome do produto inválido")
        String productName,

        @NotNull(message = "Informe a descrição pro produto")
        @Size(min = 3, message = "A descrição do produto deve ter no minimo 3 caracteres")
        String description,

        @NotNull(message = "Informe o preço base")
        @Positive(message = "Preço base tem que ser maior que zero")
        BigDecimal basePrice,

        @NotEmpty(message = "O produto precisa ter ao menos uma variante cadastrada")
        List<CreateVariantRequest> variants
) {
}