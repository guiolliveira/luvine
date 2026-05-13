package com.javacore.spring_api_luvine.auth.service;

import com.javacore.spring_api_luvine.auth.domain.entity.RefreshToken;
import com.javacore.spring_api_luvine.auth.domain.exception.*;
import com.javacore.spring_api_luvine.auth.dto.*;
import com.javacore.spring_api_luvine.auth.mapper.AuthMapper;
import com.javacore.spring_api_luvine.auth.repository.RefreshTokenRepository;
import com.javacore.spring_api_luvine.shared.dto.MessageResponse;
import com.javacore.spring_api_luvine.shared.messaging.dto.EmailMessageRequest;
import com.javacore.spring_api_luvine.shared.messaging.service.producer.ProducerService;
import com.javacore.spring_api_luvine.shared.util.EmailMask;
import com.javacore.spring_api_luvine.shared.util.TokenHash;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Name;
import com.javacore.spring_api_luvine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String maskedEmail = EmailMask.mask(request.email());
        log.info("event=register_attempt email={}", maskedEmail);

        Email email = new Email(request.email());
        Name firstName = new Name(request.firstName());
        Name lastName = new Name(request.lastName());

        if (userRepository.existsByEmail(email)) {
            log.warn("event=register_rejected reason=email_already_exists email={}", maskedEmail);
            throw new EmailAlreadyExistsException();
        }

        if (!request.password().equals(request.confirmPassword())) {
            log.warn("event=register_rejected reason=password_mismatch email={}", maskedEmail);
            throw new PasswordMisMatchException();
        }

        User user = User.create(
                email,
                firstName,
                lastName,
                passwordEncoder.encode(request.password()),
                UserProvider.LOCAL
        );

        userRepository.save(user);
        log.info("event=user_created publicId={} email={}", user.getPublicId(), maskedEmail);

        EmailVerificationCreationResult verification = verificationService.createCode(user);

        producerService.producer(new EmailMessageRequest(
                user.getEmail().value(),
                user.getFirstName().value(),
                verification.rawCode()
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
                .orElseThrow(() -> {
                    log.warn("event=token_refresh_rejected reason=token_not_found");
                    return new InvalidRefreshTokenException();
                });

        if (token.isRevoked()) {
            log.warn("event=token_refresh_rejected reason=token_revoked publicId={} — revoking all user tokens",
                    token.getUser().getPublicId());
            revokedAllUserTokens(token.getUser());
            throw new InvalidRefreshTokenException();
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            log.warn("event=token_refresh_rejected reason=token_expired publicId={}", token.getUser().getPublicId());
            throw new InvalidRefreshTokenException();
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

    public MessageResponse verifyEmail(VerifyEmailRequest request) {
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
        return new MessageResponse("Email verificado com sucesso!");
    }

    public MessageResponse resendEmail(ResendEmailRequest request) {
        String maskedEmail = EmailMask.mask(request.email());
        log.info("event=resend_verification_email_attempt email={}", maskedEmail);

        User user = findUserByEmailOrThrow(request.email());

        EmailVerificationCreationResult verification = verificationService.createCode(user);

        producerService.producer(new EmailMessageRequest(
                user.getEmail().value(),
                user.getFirstName().value(),
                verification.rawCode()
        ));

        log.info("event=resend_verification_email_queued publicId={} email={}", user.getPublicId(), maskedEmail);
        return new MessageResponse("Email de verificação reenviado com sucesso!");
    }

    private User findUserByEmailOrThrow(String email) {
        Email normalized = new Email(email);

        return userRepository.findByEmail(normalized)
                .orElseThrow(InvalidCredentialsException::new);
    }

    private void revokedAllUserTokens(User user) {
        var tokens = refreshTokenRepository.findAllByUser(user);

        for (var t : tokens) {
            t.revoke();
        }

        log.warn("event=all_tokens_revoked publicId={} count={}", user.getPublicId(), tokens.size());
    }
}