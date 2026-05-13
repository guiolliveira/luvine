package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class InvalidCepException extends BusinessException {
    public InvalidCepException() {
        super(
                "CEP é inválido",
                ErrorCode.INVALID_CEP
        );
    }
}
