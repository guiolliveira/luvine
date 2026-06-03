package com.javacore.spring_api_luvine.user.domain.valueObject;

import com.javacore.spring_api_luvine.common.util.Name;
import com.javacore.spring_api_luvine.user.domain.exception.InvalidNameException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PersonName(@Column String value) {
    public PersonName(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidNameException();
        }

        String normalized = Name.normalize(value);

        if (!Name.isValid(normalized)) {
            throw new InvalidNameException();
        }

        this.value = normalized;
    }
}