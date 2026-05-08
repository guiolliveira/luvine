package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class InvalidNameException extends BusinessException {
    public InvalidNameException() {
        super(
                "Nome é inválido",
                ErrorCode.INVALID_NAME
        );
    }
}
