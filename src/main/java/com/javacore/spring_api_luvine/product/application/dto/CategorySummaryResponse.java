package com.javacore.spring_api_luvine.product.application.dto;

import java.util.UUID;

public record CategorySummaryResponse(
        UUID publicId,
        String categoryName,
        String slug,
        String imageUrl
) {
}