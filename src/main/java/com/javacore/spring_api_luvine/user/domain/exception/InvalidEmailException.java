package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class InvalidEmailException extends BusinessException {
    public InvalidEmailException() {
        super(
                "Email é inválido",
                ErrorCode.INVALID_EMAIL
        );
    }
}
