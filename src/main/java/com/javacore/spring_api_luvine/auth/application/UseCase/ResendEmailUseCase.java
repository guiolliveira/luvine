package com.javacore.spring_api_luvine.auth.application.UseCase;

import com.javacore.spring_api_luvine.auth.application.dto.EmailVerificationCreationResult;
import com.javacore.spring_api_luvine.auth.application.dto.ResendEmailRequest;
import com.javacore.spring_api_luvine.auth.application.service.EmailVerificationService;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidCredentialsException;
import com.javacore.spring_api_luvine.auth.infrastructure.repository.PasswordResetTokenRepository;
import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.common.messaging.dto.EmailMessageRequest;
import com.javacore.spring_api_luvine.common.messaging.service.producer.ProducerService;
import com.javacore.spring_api_luvine.common.util.EmailMask;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ResendEmailUseCase {

    private final EmailVerificationService verificationService;
    private final ProducerService producerService;
    private final UserRepository userRepository;

    public void execute(ResendEmailRequest request) {
        Email email = new Email(request.email());

        String maskedEmail = EmailMask.mask(email.value());
        log.info("event=resend_verification_email_attempt email={}", maskedEmail);

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        EmailVerificationCreationResult verification = verificationService.createCode(user);

        Map<String, Object> variables = Map.of(
                "digits", verification.rawCode().split("")
        );

        producerService.producer(new EmailMessageRequest(
                user.getEmail().value(),
                user.getFirstName().value(),
                "Email de Verificação",
                "email-verification-template",
                variables
        ));

        log.info("event=resend_verification_email_queued publicId={} email={}", user.getPublicId(), maskedEmail);
    }
}