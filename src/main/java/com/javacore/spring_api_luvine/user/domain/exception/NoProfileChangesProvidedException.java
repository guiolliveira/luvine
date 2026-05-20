package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class NoProfileChangesProvidedException extends BusinessException {
    public NoProfileChangesProvidedException() {
        super(
                "Informe ao menos um campo para atualização",
                ErrorCode.NO_PROFILE_CHANGES_PROVIDED
        );
    }
}
