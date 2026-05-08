package com.javacore.spring_api_luvine.shared.exception;

public class TokenHashException extends BusinessException {
    public TokenHashException() {
        super(
                "Erro Inesperado",
                ErrorCode.TOKEN_HASH
        );
    }
}
