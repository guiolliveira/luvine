package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class InvalidColorException extends BusinessException {
    public InvalidColorException() {
        super(
                "Cor inválida",
                ErrorCode.INVALID_COLOR
        );
    }
}
