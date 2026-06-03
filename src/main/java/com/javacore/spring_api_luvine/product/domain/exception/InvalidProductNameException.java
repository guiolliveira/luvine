package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class InvalidProductNameException extends BusinessException {
    public InvalidProductNameException() {
        super(
                "Nome do produto é inválido",
                ErrorCode.INVALID_PRODUCT_NAME
        );
    }
}
