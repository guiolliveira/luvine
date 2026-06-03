package com.javacore.spring_api_luvine.product.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummaryResponse(
        UUID publicId,
        String productName,
        String slug,

        BigDecimal lowestPrice,

        String thumbnailUrl,

        boolean inStock
) {
}