package com.javacore.spring_api_luvine.auth.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException() {
        super(
                "Email ou senha incorretos",
                ErrorCode.INVALID_CREDENTIALS
        );
    }
}
