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
    private final ProducerService producerService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetTokenService passwordResetTokenService;

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