package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class ProductAlreadyExistsException extends BusinessException {
    public ProductAlreadyExistsException() {
        super(
                "Esse produto já existe",
                ErrorCode.PRODUCT_ALREADY_EXISTS
        );
    }
}
