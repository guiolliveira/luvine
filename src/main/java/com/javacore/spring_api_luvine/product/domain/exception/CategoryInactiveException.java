package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class CategoryInactiveException extends BusinessException {
    public CategoryInactiveException() {
        super(
                "Você não pode cadastrar o produto em uma categoria inativa",
                ErrorCode.CATEGORY_INACTIVE
        );
    }
}