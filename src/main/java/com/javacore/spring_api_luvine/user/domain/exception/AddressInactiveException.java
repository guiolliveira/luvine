package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class AddressInactiveException extends BusinessException {
    public AddressInactiveException() {
        super(
                "Não é possível definir um endereço inativo como padrão",
                ErrorCode.ADDRESS_INACTIVE
        );
    }
}
