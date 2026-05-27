package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidAltTextException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record AltText(@Column String value) {
    public AltText(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidAltTextException();
        }

        String normalized = normalize(value);

        if (!isValid(normalized)) {
            throw new InvalidAltTextException();
        }

        this.value = normalized;
    }

    private static String normalize(String altText) {
        return altText.trim().replaceAll("\\s+", " ");
    }

    private static boolean isValid(String altText) {
        return !altText.isBlank() && altText.length() >= 3 && altText.length() <= 150;
    }
}
