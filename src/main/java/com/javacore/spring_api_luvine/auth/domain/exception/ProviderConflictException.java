package com.javacore.spring_api_luvine.auth.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class ProviderConflictException extends BusinessException {
    public ProviderConflictException() {
        super(
                "Método de autenticação não permitido para essa conta",
                ErrorCode.PROVIDER_CONFLICT
        );
    }
}
