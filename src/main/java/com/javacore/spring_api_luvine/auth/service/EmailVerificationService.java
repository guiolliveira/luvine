package com.javacore.spring_api_luvine.auth.service;

import com.javacore.spring_api_luvine.auth.domain.entity.EmailVerification;
import com.javacore.spring_api_luvine.auth.domain.exception.EmailAlreadyVerifiedException;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidCodeException;
import com.javacore.spring_api_luvine.auth.domain.exception.RateLimitExcedeedException;
import com.javacore.spring_api_luvine.auth.dto.EmailVerificationCreationResult;
import com.javacore.spring_api_luvine.auth.repository.EmailVerificationRepository;
import com.javacore.spring_api_luvine.shared.limiter.service.RateLimiterService;
import com.javacore.spring_api_luvine.shared.util.GenerateCode;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
        Instant now = Instant.now();

        if (user.getLastVerificationEmailSentAt() != null &&
                user.getLastVerificationEmailSentAt().isBefore(now.minus(1, ChronoUnit.HOURS))) {
            user.resetEmailVerificationRequests();
        }

        int requestCount = user.getVerificationEmailRequestCount() == null ?
                0 : user.getVerificationEmailRequestCount();

        if (user.getLastVerificationEmailSentAt() != null) {
            long delaySeconds = (requestCount + 1) * 120L;
            Instant nextAllowedTime = user.getLastVerificationEmailSentAt().plusSeconds(delaySeconds);

            if (now.isBefore(nextAllowedTime)) {
                throw new RateLimitExcedeedException();
            }
        }

        var probe = rateLimiterService.tryConsume(user.getPublicId());

        if (!probe.isConsumed()) {
            throw new RateLimitExcedeedException();
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

        return new EmailVerificationCreationResult(emailVerification, rawCode);
    }

    @Transactional
    public void validateCode(Long userId, String code) {
        EmailVerification verification =
                verificationRepository.findFirstByUserIdAndVerificationCodeOrderByCreatedAtDesc(userId)
                        .orElseThrow(InvalidCodeException::new);

        if (!passwordEncoder.matches(code, verification.getVerificationCode())) {
            throw new InvalidCodeException();
        }

        User user = verification.getUser();

        if (verification.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidCodeException();
        }

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException();
        }

        verification.markEmailAsUsed();
        verificationRepository.save(verification);

        user.markEmailAsVerified();
        user.resetEmailVerificationRequests();
        userRepository.save(user);
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpiresCodes() {
        verificationRepository.deleteExpiresCode(Instant.now());
    }
}