package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class InvalidMoneyException extends BusinessException {
    public InvalidMoneyException() {
        super(
                "Valor monetário é inválido",
                ErrorCode.INVALID_MONEY
        );
    }
}
