package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class AddressInactiveException extends BusinessException {
    public AddressInactiveException() {
        super(
                "Não é possível definir um endereço inativo como padrão",
                ErrorCode.ADDRESS_INACTIVE
        );
    }
}
