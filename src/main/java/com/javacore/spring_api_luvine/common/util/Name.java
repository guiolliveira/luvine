package com.javacore.spring_api_luvine.common.util;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnsupportedOperationException;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class Name {
    private Name() {
        throw new UnsupportedOperationException();
    }

    public static String normalize(String name) {
        String[] words = name.trim().toLowerCase().split("\\s+");

        return Arrays
                .stream(words)
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static boolean isValid(String name) {
        return !name.isBlank()
                && name.length() >= 2
                && name.length() <= 100
                && name.matches("^[\\p{L} ]+$");
    }
}