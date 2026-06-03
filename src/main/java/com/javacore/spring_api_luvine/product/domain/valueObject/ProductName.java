package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidProductNameException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record ProductName(@Column String value) {
    public ProductName(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidProductNameException();
        }

        String normalized = normalize(value);

        if (!isValid(normalized)) {
            throw new InvalidProductNameException();
        }

        this.value = normalized;
    }

    private static String normalize(String productName) {
        return productName.trim().replaceAll("\\s+", " ");
    }

    private static boolean isValid(String productName) {
        return !productName.isBlank()
                && productName.length() >= 3
                && productName.matches("^[\\p{L}\\d\\s\\-.,()]+$");
    }
}