package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class UserSessionInvalidException extends BusinessException {
    public UserSessionInvalidException() {
        super(
                "Sessão inválida ou expirada",
                ErrorCode.USER_SESSION_INVALID
        );
    }
}