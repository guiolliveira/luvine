package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class CategoryAlreadyExistsException extends BusinessException {
    public CategoryAlreadyExistsException() {
        super(
                "Essa categoria já existe",
                ErrorCode.CATEGORY_ALREADY_EXISTS
        );
    }
}
