package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class ImageLimitExceededException extends BusinessException {
    public ImageLimitExceededException() {
        super(
                "Você atingiu o limite máximo de imagens permitidos nesse produto",
                ErrorCode.IMAGE_LIMIT_EXCEEDED
        );
    }
}
