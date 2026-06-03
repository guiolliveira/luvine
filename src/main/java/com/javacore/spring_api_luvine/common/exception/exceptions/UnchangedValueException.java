package com.javacore.spring_api_luvine.common.exception.exceptions;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class UnchangedValueException extends BusinessException {
    public UnchangedValueException(String message) {
        super(
                message,
                ErrorCode.UNCHANGED_VALUE
        );
    }
}
