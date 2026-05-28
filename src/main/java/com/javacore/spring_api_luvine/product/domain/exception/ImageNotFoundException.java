package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class ImageNotFoundException extends BusinessException {
    public ImageNotFoundException() {
        super(
                "Imagem não encontrada",
                ErrorCode.IMAGE_NOT_FOUND
        );
    }
}
