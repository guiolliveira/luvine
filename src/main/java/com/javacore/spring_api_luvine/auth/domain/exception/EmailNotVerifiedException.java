package com.javacore.spring_api_luvine.auth.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class EmailNotVerifiedException extends BusinessException {
    public EmailNotVerifiedException() {
        super(
                "Não foi possível concluir a solicitação com as informações fornecidas",
                ErrorCode.EMAIL_NOT_VERIFIED
        );
    }
}