package com.javacore.spring_api_luvine.auth.application.UseCase;

import com.javacore.spring_api_luvine.auth.application.dto.ForgotPasswordRequest;
import com.javacore.spring_api_luvine.auth.application.service.PasswordResetTokenService;
import com.javacore.spring_api_luvine.auth.domain.exception.EmailNotVerifiedException;
import com.javacore.spring_api_luvine.auth.infrastructure.repository.PasswordResetTokenRepository;
import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.common.messaging.dto.EmailMessageRequest;
import com.javacore.spring_api_luvine.common.messaging.service.producer.ProducerService;
import com.javacore.spring_api_luvine.common.util.EmailMask;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ForgotPasswordUseCase {

    private final UserRepository userRepository;
    private final ProducerService producerService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetTokenService passwordResetTokenService;

    @Transactional
    public void execute(ForgotPasswordRequest request, String deviceInfo, String ipAddress) {
        String maskedEmail = EmailMask.mask(request.email());
        log.info("event=forgot_password_attempt email={}", maskedEmail);

        userRepository.findByEmail(new Email(request.email()))
                .ifPresent(user -> {
                    if (!user.isEmailVerified()) {
                        log.warn("event=forgot_password_rejected reason=email_not_verified publicId={} email={}",
                                user.getPublicId(), maskedEmail);
                        throw new EmailNotVerifiedException();
                    }

                    passwordResetTokenRepository.revokeAllUserTokens(user);

                    String rawCode = passwordResetTokenService
                            .generatePasswordResetToken(user, deviceInfo, ipAddress);

                    Map<String, Object> variables = Map.of(
                            "recoveryLink", "http://localhost:8080/reset-password?token=" + rawCode
                    );

                    producerService.producer(new EmailMessageRequest(
                            user.getEmail().value(),
                            user.getFirstName().value(),
                            "Recupere a sua Conta",
                            "password-reset-template",
                            variables
                    ));

                    log.info("event=forgot_password_email_queued publicId={} email={}",
                            user.getPublicId(), maskedEmail);
                });

        log.info("event=forgot_password_processed email={}", maskedEmail);
    }
}