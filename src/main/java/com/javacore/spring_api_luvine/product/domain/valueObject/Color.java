package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.common.util.Name;
import com.javacore.spring_api_luvine.product.domain.exception.InvalidColorException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Color(@Column String value) {
    public Color(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidColorException();
        }

        String normalized = Name.normalize(value);

        if (!isValid(normalized)) {
            throw new InvalidColorException();
        }

        this.value = normalized;
    }

    private static boolean isValid(String color) {
        return !color.isBlank()
                && color.length() >= 2
                && color.length() <= 50
                && color.matches("^[\\p{L} ]+$");
    }
}