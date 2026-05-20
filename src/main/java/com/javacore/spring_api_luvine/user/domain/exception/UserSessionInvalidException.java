package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class UserSessionInvalidException extends BusinessException {
    public UserSessionInvalidException() {
        super(
                "Sessão inválida ou expirada",
                ErrorCode.USER_SESSION_INVALID
        );
    }
}