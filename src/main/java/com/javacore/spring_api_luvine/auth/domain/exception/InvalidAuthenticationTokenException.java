package com.javacore.spring_api_luvine.auth.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class InvalidAuthenticationTokenException extends BusinessException {
    public InvalidAuthenticationTokenException() {
        super(
                "Não foi possível validar as credenciais informadas",
                ErrorCode.INVALID_AUTHENTICATION_TOKEN
        );
    }
}
