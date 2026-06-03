package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class InvalidCategoryName extends BusinessException {
    public InvalidCategoryName() {
        super(
                "O nome da categoria é inválido",
                ErrorCode.INVALID_CATEGORY_NAME
        );
    }
}