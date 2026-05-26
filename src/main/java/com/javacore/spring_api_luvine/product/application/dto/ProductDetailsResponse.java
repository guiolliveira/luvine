package com.javacore.spring_api_luvine.product.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductDetailsResponse(
        UUID publicId,
        String productName,
        String description,
        String slug,

        CategorySummaryResponse category,

        List<ProductVariantResponse> variants,

        Instant createdAt
) {
}