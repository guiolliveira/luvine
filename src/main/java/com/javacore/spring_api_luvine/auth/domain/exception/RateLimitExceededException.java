package com.javacore.spring_api_luvine.auth.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class RateLimitExceededException extends BusinessException {
    public RateLimitExceededException() {
        super(
                "Tente novamente mais tarde",
                ErrorCode.RATE_LIMIT_EXCEEDED
        );
    }
}
