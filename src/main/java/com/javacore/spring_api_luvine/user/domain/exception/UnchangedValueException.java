package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class UnchangedValueException extends BusinessException {
    public UnchangedValueException() {
        super(
                "O novo valor informado deve ser diferente do valor atual",
                ErrorCode.UNCHANGED_VALUE
        );
    }
}
