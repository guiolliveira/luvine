package com.javacore.spring_api_luvine.product.application.dto;

import java.util.UUID;

public record CategoryImageResponse(
        UUID publicId,
        String imageUrl,
        String altText
) {
}