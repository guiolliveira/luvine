package com.javacore.spring_api_luvine.auth.application.usecase;

import com.javacore.spring_api_luvine.auth.application.dto.VerifyEmailRequest;
import com.javacore.spring_api_luvine.auth.application.service.EmailVerificationService;
import com.javacore.spring_api_luvine.auth.domain.exception.EmailAlreadyVerifiedException;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidCredentialsException;
import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.common.util.EmailMask;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class VerifyEmailUseCase {

    private final UserRepository userRepository;
    private final EmailVerificationService verificationService;

    public void execute(VerifyEmailRequest request) {
        Email email = new Email(request.email());

        String maskedEmail = EmailMask.mask(email.value());
        log.info("event=email_verification_attempt email={}", maskedEmail);

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (user.isEmailVerified()) {
            log.warn("event=email_verification_rejected reason=already_verified publicId={} email={}",
                    user.getPublicId(), maskedEmail);
            throw new EmailAlreadyVerifiedException();
        }

        verificationService.validateCode(user.getId(), request.code());

        log.info("event=email_verified publicId={} email={}", user.getPublicId(), maskedEmail);
    }
}