package com.javacore.spring_api_luvine.auth.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class EmailAlreadyVerifiedException extends BusinessException {
    public EmailAlreadyVerifiedException() {
        super(
                "Não foi possível concluir esta solicitação",
                ErrorCode.EMAIL_ALREADY_VERIFIED
        );
    }
}
