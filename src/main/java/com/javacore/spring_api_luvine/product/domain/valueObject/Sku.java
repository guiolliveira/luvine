package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidSkuException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record Sku(@Column String value) {
    public Sku(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidSkuException();
        }

        String normalized = normalize(value);

        if (!isValid(normalized)) {
            throw new InvalidSkuException();
        }

        this.value = normalized;
    }

    public static String generate() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }

    private static String normalize(String sku) {
        return sku.trim().toUpperCase();
    }

    private static boolean isValid(String sku) {
        return !sku.isBlank()
                && sku.length() >= 3
                && sku.length() <= 50
                && sku.matches("^[A-Z0-9-]+$");
    }
}