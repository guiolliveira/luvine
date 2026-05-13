package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class InvalidPhoneException extends BusinessException {
    public InvalidPhoneException() {
        super(
                "Número inválido",
                ErrorCode.INVALID_PHONE
        );
    }
}
