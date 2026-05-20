package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import com.javacore.spring_api_luvine.common.exception.ErrorCode;

public class SelfPromotionNotAllowedException extends BusinessException {
    public SelfPromotionNotAllowedException() {
        super(
                "Você não pode modificar suas próprias permissões",
                ErrorCode.SELF_PROMOTION_NOT_ALLOWED
        );
    }
}
