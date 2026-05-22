package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidSizeException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Size(@Column String value) {
    public Size(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidSizeException();
        }

        String normalized = normalize(value);

        if (!isValid(normalized)) {
            throw new InvalidSizeException();
        }

        this.value = normalized;
    }

    private static String normalize(String size) {
        return size.trim().toUpperCase().replaceAll("\\s+", "");
    }

    private static boolean isValid(String size) {
        return !size.isBlank()
                && size.length() >= 20
                && size.matches("^[A-Z0-9 ]+$");
    }
}