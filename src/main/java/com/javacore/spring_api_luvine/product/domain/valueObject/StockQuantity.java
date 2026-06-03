package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidStockQuantity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record StockQuantity(@Column int value) {
    public StockQuantity {
        if (value < 0) {
            throw new InvalidStockQuantity();
        }
    }
}