package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class CategoryAlreadyDeactivateException extends BusinessException {
    public CategoryAlreadyDeactivateException() {
        super(
                "Essa categoria já está desativada",
                ErrorCode.CATEGORY_ALREADY_DISABLE
        );
    }
}