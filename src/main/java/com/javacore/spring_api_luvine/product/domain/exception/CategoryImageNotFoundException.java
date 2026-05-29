package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class CategoryImageNotFoundException extends BusinessException {
    public CategoryImageNotFoundException() {
        super(
                "Imagem da categoria não encontrada",
                ErrorCode.CATEGORY_IMAGE_NOT_FOUND
        );
    }
}
