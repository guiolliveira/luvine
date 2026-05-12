package com.javacore.spring_api_luvine.auth.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class InvalidAuthenticationTokenException extends BusinessException {
    public InvalidAuthenticationTokenException() {
        super(
                "Não foi possível validar as credenciais informadas",
                ErrorCode.INVALID_AUTHENTICATION_TOKEN
        );
    }
}
