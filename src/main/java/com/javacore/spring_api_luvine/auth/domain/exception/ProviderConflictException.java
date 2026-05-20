package com.javacore.spring_api_luvine.auth.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class ProviderConflictException extends BusinessException {
    public ProviderConflictException() {
        super(
                "Método de autenticação não permitido para essa conta",
                ErrorCode.PROVIDER_CONFLICT
        );
    }
}
