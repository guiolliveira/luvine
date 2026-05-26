package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class ImageNotPrimaryException extends BusinessException {
    public ImageNotPrimaryException() {
        super(
                "Está imagem não é a principal",
                ErrorCode.IMAGE_NOT_PRIMARY
        );
    }
}
