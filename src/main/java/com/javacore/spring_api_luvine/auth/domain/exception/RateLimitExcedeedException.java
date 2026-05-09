package com.javacore.spring_api_luvine.auth.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class RateLimitExcedeedException extends BusinessException {
    public RateLimitExcedeedException() {
        super(
                "Tente novamente mais tarde",
                ErrorCode.RATE_LIMIT_EXCEDEED
        );
    }
}
