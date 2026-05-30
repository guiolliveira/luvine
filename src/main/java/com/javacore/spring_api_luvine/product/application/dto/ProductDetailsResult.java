package com.javacore.spring_api_luvine.product.application.dto;

public record ProductDetailsResult(
        ProductDetailsResponse response,
        String currentSlug
) {
}