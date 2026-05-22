package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException() {
        super(
                "Estoque insuficiente",
                ErrorCode.INSUFFICIENT_STOCK
        );
    }
}
