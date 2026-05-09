package com.javacore.spring_api_luvine.auth.dto;

import com.javacore.spring_api_luvine.auth.domain.entity.EmailVerification;

public record EmailVerificationCreationResult(
        EmailVerification verification,
        String rawCode
) {
}
