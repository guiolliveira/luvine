package com.javacore.spring_api_luvine.auth.application.dto;

import com.javacore.spring_api_luvine.auth.domain.entity.EmailVerification;

public record EmailVerificationCreationResult(
        EmailVerification verification,
        String rawCode
) {
}
