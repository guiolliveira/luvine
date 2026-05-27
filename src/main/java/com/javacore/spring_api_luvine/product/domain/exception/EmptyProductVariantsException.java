package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class EmptyProductVariantsException extends BusinessException {
    public EmptyProductVariantsException() {
        super(
                "O produto deve possuir pelo menos uma variante",
                ErrorCode.EMPTY_PRODUCT_VARIANTS
        );
    }
}