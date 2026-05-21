package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class NoProfileChangesProvidedException extends BusinessException {
    public NoProfileChangesProvidedException() {
        super(
                "Informe ao menos um campo para atualização",
                ErrorCode.NO_PROFILE_CHANGES_PROVIDED
        );
    }
}
