package com.javacore.spring_api_luvine.product.application.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateCategoryRequest(

        @Size(max = 100, message = "O nome da categoria deve ter no máximo 100 caracteres")
        String newCategoryName,

        @Size(min = 3, message = "A descrição deve ter no minimo 3 caracteres")
        String newDescription,

        UUID newParentPublicId
) {
}