package com.javacore.spring_api_luvine.product.application.dto;

public record UploadResult(
        String imageUrl,
        String storageKey
) {
}