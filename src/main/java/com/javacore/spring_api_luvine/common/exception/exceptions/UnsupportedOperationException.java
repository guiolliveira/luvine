package com.javacore.spring_api_luvine.common.exception.exceptions;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class UnsupportedOperationException extends BusinessException {
    public UnsupportedOperationException() {
        super(
                "Operação não suportada",
                ErrorCode.UNSUPPORTED_OPERATION
        );
    }
}
