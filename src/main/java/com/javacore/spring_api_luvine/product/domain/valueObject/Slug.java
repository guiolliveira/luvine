package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidSlugException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.text.Normalizer;

@Embeddable
public record Slug(@Column String value) {
    public Slug(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidSlugException();
        }

        String normalized = normalize(value);

        if (!isValid(normalized)) {
            throw new InvalidSlugException();
        }

        this.value = normalized;
    }

    private static String normalize(String slug) {
        return Normalizer.normalize(slug, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private static boolean isValid(String slug) {
        return !slug.isBlank() && slug.length() <= 150;
    }
}