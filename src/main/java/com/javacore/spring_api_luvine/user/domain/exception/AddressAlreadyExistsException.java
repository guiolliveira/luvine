package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class AddressAlreadyExistsException extends BusinessException {
    public AddressAlreadyExistsException() {
        super(
                "Este endereço já está cadastrado",
                ErrorCode.ADDRESS_ALREADY_EXISTS
        );
    }
}