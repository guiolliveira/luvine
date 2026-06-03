package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class InvalidStockQuantity extends BusinessException {
    public InvalidStockQuantity() {
        super(
                "Estoque é inválido",
                ErrorCode.INVALID_STOCK_QUANTITY
        );
    }
}
