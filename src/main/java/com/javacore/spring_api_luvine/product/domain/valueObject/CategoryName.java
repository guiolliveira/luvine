package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.common.util.Name;
import com.javacore.spring_api_luvine.product.domain.exception.InvalidCategoryName;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record CategoryName(@Column String value) {
    public CategoryName(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidCategoryName();
        }

        String normalized = Name.normalize(value);

        if (!Name.isValid(normalized)) {
            throw new InvalidCategoryName();
        }

        this.value = normalized;
    }
}