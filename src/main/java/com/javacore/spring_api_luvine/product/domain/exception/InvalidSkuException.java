package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class InvalidSkuException extends BusinessException {
    public InvalidSkuException() {
        super(
                "Sku é inválido",
                ErrorCode.INVALID_SKU
        );
    }
}
