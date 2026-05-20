package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class RoleAlreadyAssignedException extends BusinessException {
    public RoleAlreadyAssignedException() {
        super(
                "A permissão informada já está atribuída ao usuário",
                ErrorCode.ROLE_ALREADY_ASSIGNED
        );
    }
}
