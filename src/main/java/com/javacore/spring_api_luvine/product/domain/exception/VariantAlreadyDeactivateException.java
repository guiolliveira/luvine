package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class VariantAlreadyDeactivateException extends BusinessException {
    public VariantAlreadyDeactivateException() {
        super(
                "Essa variante já está desativada",
                ErrorCode.VARIANT_ALREADY_DEACTIVATE
        );
    }
}