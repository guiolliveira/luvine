package com.javacore.spring_api_luvine.auth.application.usecase;

import com.javacore.spring_api_luvine.auth.application.dto.UpdatePasswordRequest;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidTokenException;
import com.javacore.spring_api_luvine.auth.domain.exception.PasswordMisMatchException;
import com.javacore.spring_api_luvine.auth.infrastructure.repository.PasswordResetTokenRepository;
import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.common.util.TokenHash;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.valueObject.Password;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ResetPasswordUseCase {

    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    public void execute(UpdatePasswordRequest request) {
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
}