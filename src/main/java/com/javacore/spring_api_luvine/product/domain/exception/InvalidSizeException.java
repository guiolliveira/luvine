package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class InvalidSizeException extends BusinessException {
    public InvalidSizeException() {
        super(
                "Tamanho é inválido",
                ErrorCode.INVALID_SIZE
        );
    }
}