package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class InvalidNameException extends BusinessException {
    public InvalidNameException() {
        super(
                "O nome informado é inválido",
                ErrorCode.INVALID_NAME
        );
    }
}