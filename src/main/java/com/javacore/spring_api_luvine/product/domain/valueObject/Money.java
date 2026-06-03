package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidMoneyException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
public record Money(@Column BigDecimal value) {
    public Money(BigDecimal value) {
        if (!isValid(value)) {
            throw new InvalidMoneyException();
        }

        this.value = normalize(value);
    }

    private static BigDecimal normalize(BigDecimal money) {
        return money.setScale(2, RoundingMode.HALF_UP);
    }

    private static boolean isValid(BigDecimal money) {
        return money != null && money.compareTo(BigDecimal.ZERO) >= 0;
    }
}