package com.javacore.spring_api_luvine.product.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductVariantResponse(
        UUID publicId,
        String sku,
        String color,
        String size,

        BigDecimal price,

        Integer stockQuantity,

        List<ProductImageResponse> images
) {
}