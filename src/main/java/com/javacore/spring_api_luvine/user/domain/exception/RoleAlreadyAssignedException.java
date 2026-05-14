package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class RoleAlreadyAssignedException extends BusinessException {
    public RoleAlreadyAssignedException() {
        super(
                "A permissão informada já está atribuída ao usuário",
                ErrorCode.ROLE_ALREADY_ASSIGNED
        );
    }
}
