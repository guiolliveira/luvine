package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class AddressNotFoundException extends BusinessException {
    public AddressNotFoundException() {
        super(
                "Não foi possivel encontrar esse endereço",
                ErrorCode.ADDRESS_NOT_FOUND
        );
    }
}
