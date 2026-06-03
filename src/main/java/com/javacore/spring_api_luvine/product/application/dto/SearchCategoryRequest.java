package com.javacore.spring_api_luvine.product.application.dto;

import jakarta.validation.constraints.Size;

public record SearchCategoryRequest(

        @Size(max = 150, message = "O nome da categoria deve ter no máximo 150 caracteres")
        String categoryName,

        Boolean active,

        @Size(max = 150, message = "O slug deve ter no máximo 150 caracteres")
        String parentSlug,

        Boolean rootOnly
) {
}