package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class InvalidPasswordException extends BusinessException {
    public InvalidPasswordException() {
        super(
                "A senha informada é inválida",
                ErrorCode.INVALID_PASSWORD
        );
    }
}
