package com.javacore.spring_api_luvine.auth.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class PasswordMisMatchException extends BusinessException {
    public PasswordMisMatchException() {
        super(
                "As senhas precisam ser iguais",
                ErrorCode.PASSWORD_MISMATCH
        );
    }
}
