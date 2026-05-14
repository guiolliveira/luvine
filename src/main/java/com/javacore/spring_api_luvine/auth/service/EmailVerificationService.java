package com.javacore.spring_api_luvine.auth.service;

import com.javacore.spring_api_luvine.auth.domain.entity.EmailVerification;
import com.javacore.spring_api_luvine.auth.domain.exception.EmailAlreadyVerifiedException;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidCodeException;
import com.javacore.spring_api_luvine.auth.domain.exception.RateLimitExceededException;
import com.javacore.spring_api_luvine.auth.dto.EmailVerificationCreationResult;
import com.javacore.spring_api_luvine.auth.repository.EmailVerificationRepository;
import com.javacore.spring_api_luvine.shared.limiter.service.RateLimiterService;
import com.javacore.spring_api_luvine.shared.util.GenerateCode;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiterService rateLimiterService;
    private final GenerateCode generateCode;

    @Transactional
    public EmailVerificationCreationResult createCode(User user) {
        log.debug("event=verification_code_create_attempt publicId={}", user.getPublicId());

        Instant now = Instant.now();

        if (user.getLastVerificationEmailSentAt() != null &&
                user.getLastVerificationEmailSentAt().isBefore(now.minus(1, ChronoUnit.HOURS))) {
            user.resetEmailVerificationRequests();
            log.debug("event=verification_rate_reset publicId={}", user.getPublicId());
        }

        int requestCount = user.getVerificationEmailRequestCount() == null ?
                0 : user.getVerificationEmailRequestCount();

        if (user.getLastVerificationEmailSentAt() != null) {
            long delaySeconds = (requestCount + 1) * 120L;
            Instant nextAllowedTime = user.getLastVerificationEmailSentAt().plusSeconds(delaySeconds);

            if (now.isBefore(nextAllowedTime)) {
                log.warn("event=verification_code_rejected reason=cooldown_active publicId={} requestCount={}",
                        user.getPublicId(), requestCount);
                throw new RateLimitExceededException();
            }
        }

        var probe = rateLimiterService.tryConsume(user.getPublicId());

        if (!probe.isConsumed()) {
            log.warn("event=verification_code_rejected reason=rate_limit_bucket_exhausted publicId={} remainingTokens={}",
                    user.getPublicId(), probe.getRemainingTokens());
            throw new RateLimitExceededException();
        }

        user.markVerificationEmailSent();
        userRepository.save(user);

        verificationRepository.markAllCodesUsedForUser(user.getId());

        String rawCode = generateCode.generate();

        EmailVerification emailVerification = EmailVerification.create(
                user,
                passwordEncoder.encode(rawCode)
        );

        verificationRepository.save(emailVerification);

        log.info("event=verification_code_created publicId={} requestCount={}", user.getPublicId(), requestCount + 1);
        return new EmailVerificationCreationResult(emailVerification, rawCode);
    }

    @Transactional
    public void validateCode(Long userId, String code) {
        log.debug("event=verification_code_validate_attempt userId={}", userId);

        EmailVerification verification =
                verificationRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(userId)
                        .filter(v -> v.getExpiresAt().isAfter(Instant.now()))
                        .orElseThrow(() -> {
                            log.warn("event=verification_code_rejected reason=no_active_code userId={}", userId);
                            return new InvalidCodeException();
                        });

        if (!passwordEncoder.matches(code, verification.getVerificationCode())) {
            log.warn("event=verification_code_rejected reason=code_mismatch userId={}", userId);
            throw new InvalidCodeException();
        }

        User user = verification.getUser();

        if (user.isEmailVerified()) {
            log.warn("event=verification_code_rejected reason=email_already_verified publicId={}", user.getPublicId());
            throw new EmailAlreadyVerifiedException();
        }

        verification.markEmailAsUsed();
        verificationRepository.save(verification);

        user.markEmailAsVerified();
        user.resetEmailVerificationRequests();
        userRepository.save(user);

        log.info("event=email_verification_success publicId={}", user.getPublicId());
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpiresCodes() {
        log.info("event=cleanup_expired_verification_codes_started");
        verificationRepository.deleteExpiredCode(Instant.now());
        log.info("event=cleanup_expired_verification_codes_completed");
    }
}