package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class ImageAlreadyPrimaryException extends BusinessException {
    public ImageAlreadyPrimaryException() {
        super(
                "Esta imagem já está definida como principal",
                ErrorCode.IMAGE_ALREADY_PRIMARY
        );
    }
}
