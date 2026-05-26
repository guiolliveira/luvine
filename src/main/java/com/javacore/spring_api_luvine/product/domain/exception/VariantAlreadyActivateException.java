package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class VariantAlreadyActivateException extends BusinessException {
    public VariantAlreadyActivateException() {
        super(
                "Essa variante já está ativa",
                ErrorCode.VARIANT_ALREADY_ACTIVATE
        );
    }
}
