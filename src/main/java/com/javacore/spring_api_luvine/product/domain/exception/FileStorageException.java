package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class FileStorageException extends BusinessException {
    public FileStorageException() {
        super(
                "Não foi possível processar a operação de armazenamento no momento",
                ErrorCode.FILE_STORAGE
        );
    }
}
