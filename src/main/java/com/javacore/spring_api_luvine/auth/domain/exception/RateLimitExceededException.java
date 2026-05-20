package com.javacore.spring_api_luvine.auth.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class RateLimitExceededException extends BusinessException {
    public RateLimitExceededException() {
        super(
                "Tente novamente mais tarde",
                ErrorCode.RATE_LIMIT_EXCEEDED
        );
    }
}
