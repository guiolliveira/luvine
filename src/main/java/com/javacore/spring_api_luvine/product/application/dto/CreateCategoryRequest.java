package com.javacore.spring_api_luvine.product.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCategoryRequest(
        UUID parentPublicId,

        @NotBlank(message = "Informe o nome da categoria")
        @Size(max = 100, message = "O nome da categoria deve ter no máximo 100 caracteres")
        String categoryName,

        @NotBlank(message = "Informe a descrição")
        @Size(min = 3, message = "A descrição deve ter no minimo 3 caracteres")
        String description
) {
}