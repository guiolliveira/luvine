package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidDescriptionException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Description(@Column String value) {
    public Description(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidDescriptionException();
        }

        String normalized = normalize(value);

        if (!isValid(normalized)) {
            throw new InvalidDescriptionException();
        }

        this.value = normalized;
    }

    private static String normalize(String description) {
        return description.trim().replaceAll("\\s+", " ");
    }

    private static boolean isValid(String description) {
        return !description.isBlank() && description.length() >= 3;
    }
}