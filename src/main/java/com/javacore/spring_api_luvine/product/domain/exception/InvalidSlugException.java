package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class InvalidSlugException extends BusinessException {
    public InvalidSlugException() {
        super(
                "Slug é inválido",
                ErrorCode.INVALID_SLUG
        );
    }
}