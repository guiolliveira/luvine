package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class InsufficientPromotionsException extends BusinessException {
    public InsufficientPromotionsException() {
        super(
                "Você não possui permissão para realizar esta operação",
                ErrorCode.INSUFFICIENT_PERMISSIONS
        );
    }
}
