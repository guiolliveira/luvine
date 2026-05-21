package com.javacore.spring_api_luvine.user.domain.valueObject;

import com.javacore.spring_api_luvine.user.domain.exception.InvalidPasswordException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Password(@Column String value) {
    public Password(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidPasswordException();
        }

        String normalized = normalize(value);

        if (!isValid(normalized)) {
            throw new InvalidPasswordException();
        }

        this.value = normalized;
    }

    private static String normalize(String password) {
        return password.trim();
    }

    private static boolean isValid(String password) {
        return !password.isBlank()
                && password.length() >= 8
                && password.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$");
    }
}