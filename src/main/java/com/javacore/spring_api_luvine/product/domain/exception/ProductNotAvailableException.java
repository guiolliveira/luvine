package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class ProductNotAvailableException extends BusinessException {
    public ProductNotAvailableException() {
        super(
                "Esse produto não está disponivel no momento",
                ErrorCode.PRODUCT_NOT_AVAILABLE
        );
    }
}
