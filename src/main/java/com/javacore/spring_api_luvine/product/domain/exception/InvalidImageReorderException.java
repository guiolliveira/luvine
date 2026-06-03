package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class InvalidImageReorderException extends BusinessException {
    public InvalidImageReorderException() {
        super(
                "A ordem informada para as imagens é inválida",
                ErrorCode.INVALID_IMAGE_REORDER
        );
    }
}
