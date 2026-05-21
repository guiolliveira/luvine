package com.javacore.spring_api_luvine.auth.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException() {
        super(
                "Não foi possível processar sua solicitação com as informações fornecidas",
                ErrorCode.EMAIL_ALREADY_EXISTS
        );
    }
}
