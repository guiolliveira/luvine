package com.javacore.spring_api_luvine.user.domain.exception;

import com.javacore.spring_api_luvine.shared.exception.BusinessException;
import com.javacore.spring_api_luvine.shared.exception.ErrorCode;

public class SelfPromotionNotAllowedException extends BusinessException {
    public SelfPromotionNotAllowedException() {
        super(
                "Você não pode modificar suas próprias permissões",
                ErrorCode.SELF_PROMOTION_NOT_ALLOWED
        );
    }
}
