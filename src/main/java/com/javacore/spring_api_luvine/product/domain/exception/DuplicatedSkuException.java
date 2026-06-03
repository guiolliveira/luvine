package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class DuplicatedSkuException extends BusinessException {
    public DuplicatedSkuException() {
        super(
                "Já existe um produto cadastrado com esse SKU",
                ErrorCode.DUPLICATED_SKU
        );
    }
}