package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class InvalidPhoneException extends BusinessException {
    public InvalidPhoneException() {
        super(
                "Número inválido",
                ErrorCode.INVALID_PHONE
        );
    }
}
