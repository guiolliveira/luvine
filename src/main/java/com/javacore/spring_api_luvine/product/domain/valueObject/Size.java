package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidSizeException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Set;

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

    private static final Set<String> VALID_SIZES = Set.of(
            "PP",
            "P",
            "M",
            "G",
            "GG",
            "XG",
            "XGG"
    );

    private static boolean isValid(String size) {
        return !size.isBlank() && VALID_SIZES.contains(size);
    }
}