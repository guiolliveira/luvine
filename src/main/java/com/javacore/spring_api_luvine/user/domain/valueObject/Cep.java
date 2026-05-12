package com.javacore.spring_api_luvine.user.domain.valueObject;

import com.javacore.spring_api_luvine.user.domain.exception.InvalidCepException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Cep(@Column String value) {
    public Cep(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidCepException();
        }

        String normalized = normalize(value);

        if (!isValid(normalized)) {
            throw new InvalidCepException();
        }

        this.value = normalized;
    }

    public String getFormatted() {
        return value.substring(0, 5) + "-" + value.substring(5);
    }

    private static String normalize(String cep) {
        return cep.replaceAll("\\D", "").trim();
    }

    private static boolean isValid(String cep) {
        return cep.matches("\\d{8}");
    }
}