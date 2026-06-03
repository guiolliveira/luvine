package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class CategoryNotFoundException extends BusinessException {
    public CategoryNotFoundException() {
        super(
                "Categoria não encontrada",
                ErrorCode.CATEGORY_NOT_FOUND
        );
    }
}
