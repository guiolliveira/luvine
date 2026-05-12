package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class AddressAlreadyExistsException extends BusinessException {
    public AddressAlreadyExistsException() {
        super(
                "Este endereço já está cadastrado",
                ErrorCode.ADDRESS_ALREADY_EXISTS
        );
    }
}