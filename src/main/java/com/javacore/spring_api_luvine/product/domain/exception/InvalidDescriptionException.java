package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class InvalidDescriptionException extends BusinessException {
    public InvalidDescriptionException() {
        super(
                "Descrição é inválida",
                ErrorCode.INVALID_DESCRIPTION
        );
    }
}