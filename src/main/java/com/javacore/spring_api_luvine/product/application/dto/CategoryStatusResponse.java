package com.javacore.spring_api_luvine.product.application.dto;

import java.util.UUID;

public record CategoryStatusResponse(
        UUID publicId,
        String categoryName,
        boolean active
) {
}