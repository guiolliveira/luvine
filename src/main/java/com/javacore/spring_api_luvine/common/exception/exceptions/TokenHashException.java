package com.javacore.spring_api_luvine.common.exception.exceptions;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class TokenHashException extends BusinessException {
    public TokenHashException() {
        super(
                "Erro Inesperado",
                ErrorCode.TOKEN_HASH
        );
    }
}
