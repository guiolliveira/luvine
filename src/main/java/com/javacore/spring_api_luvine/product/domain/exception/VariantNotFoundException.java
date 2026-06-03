package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class VariantNotFoundException extends BusinessException {
    public VariantNotFoundException() {
        super(
                "Variante do produto não encontrada",
                ErrorCode.VARIANT_NOT_FOUND
        );
    }
}
