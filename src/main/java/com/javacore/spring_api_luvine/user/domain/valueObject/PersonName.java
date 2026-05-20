package com.javacore.spring_api_luvine.user.domain.valueObject;

import com.javacore.spring_api_luvine.user.domain.exception.InvalidNameException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Arrays;
import java.util.stream.Collectors;

@Embeddable
public record PersonName(@Column String value) {
    public PersonName(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidNameException();
        }

        String normalized = normalize(value);

        if (!isValid(normalized)) {
            throw new InvalidNameException();
        }

        this.value = normalized;
    }

    private static String normalize(String name) {
        String[] words = name.trim().toLowerCase().split("\\s+");

        return Arrays
                .stream(words)
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    private static boolean isValid(String name) {
        return !name.isBlank()
                && name.length() >= 2
                && name.length() <= 100
                && name.matches("^[\\p{L} ]+$");
    }
}