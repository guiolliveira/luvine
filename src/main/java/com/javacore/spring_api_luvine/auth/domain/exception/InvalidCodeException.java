package com.javacore.spring_api_luvine.auth.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class InvalidCodeException extends BusinessException {
    public InvalidCodeException() {
        super(
                "Código inválido ou expirado",
                ErrorCode.INVALID_CODE
        );
    }
}
