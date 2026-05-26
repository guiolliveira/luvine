package com.javacore.spring_api_luvine.product.application.dto;

import java.util.List;
import java.util.UUID;

public record CategoryDetailsResponse(
        UUID publicId,
        String categoryName,
        String slug,
        String description,
        String imageUrl,

        Integer displayOrder,

        boolean active,

        UUID parentPublicId,

        String parentCategoryName
) {
}