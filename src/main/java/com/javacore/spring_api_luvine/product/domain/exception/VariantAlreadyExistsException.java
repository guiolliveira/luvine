package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class VariantAlreadyExistsException extends BusinessException {
    public VariantAlreadyExistsException() {
        super(
                "Essa variante já existe",
                ErrorCode.VARIANT_ALREADY_EXISTS
        );
    }
}
