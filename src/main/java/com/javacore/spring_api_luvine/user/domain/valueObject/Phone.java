package com.javacore.spring_api_luvine.user.domain.valueObject;

import com.javacore.spring_api_luvine.user.domain.exception.InvalidPhoneException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Phone(@Column String value) {
    public Phone(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidPhoneException();
        }

        String normalized = normalize(value);

        if (!isValid(normalized)) {
            throw new InvalidPhoneException();
        }

        this.value = normalized;
    }

    public String getFormatted() {
        if (value.length() == 11) {
            return value.replaceFirst("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
        }

        return value.replaceFirst("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
    }

    private static String normalize(String phone) {
        return phone.replaceAll("\\D", "");
    }

    private static boolean isValid(String phone) {
        return phone.matches("^[1-9]{2}\\d{8,9}$");
    }
}