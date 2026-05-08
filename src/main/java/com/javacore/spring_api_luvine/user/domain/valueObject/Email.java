package com.javacore.spring_api_luvine.user.domain.valueObject;

import com.javacore.spring_api_luvine.user.domain.exception.InvalidEmailException;

public record Email(String value) {
    public Email(String value) {
        if (value == null) {
            throw new InvalidEmailException();
        }

        String normalized = normalize(value);

        if (normalized.isBlank()) {
            throw new InvalidEmailException();
        }

        int indexAt = normalized.indexOf("@");

        if (indexAt <= 0 || indexAt == normalized.length() - 1) {
            throw new InvalidEmailException();
        }

        String local = normalized.substring(0, indexAt);
        String domain = normalized.substring(indexAt);

        if (local.isBlank() || domain.isBlank()) {
            throw new InvalidEmailException();
        }

        this.value = normalized;
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
