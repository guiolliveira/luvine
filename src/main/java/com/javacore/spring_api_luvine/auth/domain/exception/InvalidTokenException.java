package com.javacore.spring_api_luvine.auth.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException() {
        super(
                "Token inválido ou expirado",
                ErrorCode.INVALID_TOKEN
        );
    }
}