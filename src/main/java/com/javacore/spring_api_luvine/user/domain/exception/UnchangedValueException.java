package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class UnchangedValueException extends BusinessException {
    public UnchangedValueException() {
        super(
                "O novo valor informado deve ser diferente do valor atual",
                ErrorCode.UNCHANGED_VALUE
        );
    }
}
