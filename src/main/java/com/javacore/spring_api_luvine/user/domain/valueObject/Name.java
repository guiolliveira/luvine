package com.javacore.spring_api_luvine.user.domain.valueObject;

import com.javacore.spring_api_luvine.user.domain.exception.InvalidNameException;

import java.util.Arrays;
import java.util.stream.Collectors;

public record Name(String value) {
    public Name(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidNameException();
        }

        this.value = normalize(value);
    }

    private String normalize(String name) {
        String[] words = name.trim().toLowerCase().split("\\s+");

        return Arrays
                .stream(words)
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}