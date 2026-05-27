package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class InvalidAltTextException extends BusinessException {
    public InvalidAltTextException() {
        super(
                "Texto alternativo é inválido",
                ErrorCode.INVALID_ALT_TEXT
        );
    }
}