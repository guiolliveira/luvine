package com.javacore.spring_api_luvine.auth.application.usecase;

import com.javacore.spring_api_luvine.auth.application.dto.EmailVerificationCreationResult;
import com.javacore.spring_api_luvine.auth.application.dto.RegisterRequest;
import com.javacore.spring_api_luvine.auth.application.dto.RegisterResponse;
import com.javacore.spring_api_luvine.auth.application.mapper.AuthMapper;
import com.javacore.spring_api_luvine.auth.application.service.EmailVerificationService;
import com.javacore.spring_api_luvine.auth.domain.exception.EmailAlreadyExistsException;
import com.javacore.spring_api_luvine.auth.domain.exception.PasswordMisMatchException;
import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.common.messaging.dto.EmailMessageRequest;
import com.javacore.spring_api_luvine.common.messaging.service.producer.ProducerService;
import com.javacore.spring_api_luvine.common.util.EmailMask;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Password;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final EmailVerificationService verificationService;
    private final ProducerService producerService;

    @Transactional
    public RegisterResponse execute(RegisterRequest request) {
        String maskedEmail = EmailMask.mask(request.email());
        log.info("event=register_attempt email={}", maskedEmail);

        Email email = new Email(request.email());
        PersonName firstName = new PersonName(request.firstName());
        PersonName lastName = new PersonName(request.lastName());
        Password password = new Password(request.password());

        if (userRepository.existsByEmail(email)) {
            log.warn("event=register_rejected reason=email_already_exists email={}", maskedEmail);
            throw new EmailAlreadyExistsException();
        }

        if (!password.value().equals(request.confirmPassword())) {
            log.warn("event=register_rejected reason=password_mismatch email={}", maskedEmail);
            throw new PasswordMisMatchException();
        }

        User user = User.create(
                email,
                firstName,
                lastName,
                new Password(passwordEncoder.encode(password.value())),
                UserProvider.LOCAL
        );

        userRepository.save(user);
        log.info("event=user_created publicId={} email={}", user.getPublicId(), maskedEmail);

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

        log.info("event=register_completed publicId={} email={}", user.getPublicId(), maskedEmail);
        return authMapper.toRegisterResponse(user);
    }
}