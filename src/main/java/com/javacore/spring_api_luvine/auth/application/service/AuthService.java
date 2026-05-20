package com.javacore.spring_api_luvine.auth.application.service;

import com.javacore.spring_api_luvine.auth.application.dto.*;
import com.javacore.spring_api_luvine.auth.domain.entity.RefreshToken;
import com.javacore.spring_api_luvine.auth.domain.exception.*;
import com.javacore.spring_api_luvine.auth.application.mapper.AuthMapper;
import com.javacore.spring_api_luvine.auth.infrastructure.repository.PasswordResetTokenRepository;
import com.javacore.spring_api_luvine.auth.infrastructure.repository.RefreshTokenRepository;
import com.javacore.spring_api_luvine.common.messaging.dto.EmailMessageRequest;
import com.javacore.spring_api_luvine.common.messaging.service.producer.ProducerService;
import com.javacore.spring_api_luvine.common.util.EmailMask;
import com.javacore.spring_api_luvine.common.util.TokenHash;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Password;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationService verificationService;
    private final ProducerService producerService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetTokenService passwordResetTokenService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
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

    public LoginResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
        String maskedEmail = EmailMask.mask(request.email());
        log.info("event=login_attempt email={} ip={}", maskedEmail, ipAddress);

        Email email = new Email(request.email());

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    email.value(),
                    request.password()
            ));
        } catch (AuthenticationException ex) {
            log.warn("event=login_failed reason=invalid_credentials email={} ip={}", maskedEmail, ipAddress);
            throw new InvalidCredentialsException();
        }

        User user = findUserByEmailOrThrow(email.value());

        if (user.getUserProvider() != UserProvider.LOCAL) {
            log.warn("event=login_rejected reason=provider_conflict publicId={} provider={}",
                    user.getPublicId(), user.getUserProvider());
            throw new ProviderConflictException();
        }

        if (!user.isEmailVerified()) {
            log.warn("event=login_rejected reason=email_not_verified publicId={} email={}",
                    user.getPublicId(), maskedEmail);
            throw new EmailNotVerifiedException();
        }

        String refreshToken = tokenService.generateRefreshToken(user, deviceInfo, ipAddress);
        String accessToken = tokenService.generateAccessToken(user);

        log.info("event=login_success publicId={} email={} ip={}", user.getPublicId(), maskedEmail, ipAddress);
        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse refresh(String refreshToken) {
        log.debug("event=token_refresh_attempt");

        String tokenHash = TokenHash.hash(refreshToken);

        RefreshToken token = refreshTokenRepository.findByToken(tokenHash)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> {
                    log.warn("event=token_refresh_rejected reason=token_not_found");
                    return new InvalidTokenException();
                });

        if (token.isRevoked()) {
            log.warn("event=token_refresh_rejected reason=token_revoked publicId={} — revoking all user tokens",
                    token.getUser().getPublicId());
            refreshTokenRepository.revokeAllUserTokens(token.getUser());
            throw new InvalidTokenException();
        }

        token.revoke();

        String newRefreshToken = tokenService.generateRefreshToken(
                token.getUser(),
                token.getDeviceInfo(),
                token.getIpAddress()
        );

        String newTokenHash = TokenHash.hash(newRefreshToken);
        token.markAsReplacedBy(newTokenHash);

        String newAccessToken = tokenService.generateAccessToken(token.getUser());

        log.info("event=token_refreshed publicId={}", token.getUser().getPublicId());
        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    public void verifyEmail(VerifyEmailRequest request) {
        String maskedEmail = EmailMask.mask(request.email());
        log.info("event=email_verification_attempt email={}", maskedEmail);

        User user = findUserByEmailOrThrow(request.email());

        if (user.isEmailVerified()) {
            log.warn("event=email_verification_rejected reason=already_verified publicId={} email={}",
                    user.getPublicId(), maskedEmail);
            throw new EmailAlreadyVerifiedException();
        }

        verificationService.validateCode(user.getId(), request.code());

        log.info("event=email_verified publicId={} email={}", user.getPublicId(), maskedEmail);
    }

    public void resendEmail(ResendEmailRequest request) {
        String maskedEmail = EmailMask.mask(request.email());
        log.info("event=resend_verification_email_attempt email={}", maskedEmail);

        User user = findUserByEmailOrThrow(request.email());

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

    @Transactional
    public void processForgotPassword(ForgotPasswordRequest request, String deviceInfo, String ipAddress) {
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

    @Transactional
    public void resetPassword(UpdatePasswordRequest request) {
        log.info("event=reset_password_attempt");

        Password newPassword = new Password(request.newPassword());

        if (!newPassword.value().equals(request.confirmPassword())) {
            log.warn("event=reset_password_rejected reason=password_mismatch");
            throw new PasswordMisMatchException();
        }

        String tokenHash = TokenHash.hash(request.token());

        var token = passwordResetTokenRepository.findByTokenAndUsedFalseAndRevokedFalse(tokenHash)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> {
                    log.warn("event=reset_password_rejected reason=invalid_or_expired_token");
                    return new InvalidTokenException();
                });

        User user = token.getUser();
        user.changePassword(new Password(passwordEncoder.encode(newPassword.value())));

        token.markAsUsed();
        passwordResetTokenRepository.save(token);

        log.info("event=reset_password_success publicId={}", user.getPublicId());
    }

    private User findUserByEmailOrThrow(String email) {
        Email normalized = new Email(email);

        return userRepository.findByEmail(normalized)
                .orElseThrow(InvalidCredentialsException::new);
    }
}