package com.javacore.spring_api_luvine.product.application.dto;

import java.util.UUID;

public record ProductImageResponse(
        UUID publicId,
        String imageUrl,
        String altText,
        Integer displayOrder,
        boolean primaryImage
) {
}