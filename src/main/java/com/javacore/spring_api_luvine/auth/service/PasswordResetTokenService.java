package com.javacore.spring_api_luvine.auth.service;

import com.javacore.spring_api_luvine.auth.domain.entity.PasswordResetToken;
import com.javacore.spring_api_luvine.auth.repository.PasswordResetTokenRepository;
import com.javacore.spring_api_luvine.shared.util.RequestInfo;
import com.javacore.spring_api_luvine.shared.util.TokenHash;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    public String generatePasswordResetToken(User user, String deviceInfo, String ipAddress) {

        String rawCode = TokenHash.generateSecureToken();

        PasswordResetToken resetToken = PasswordResetToken.create(
                user,
                TokenHash.hash(rawCode),
                RequestInfo.truncateDeviceInfo(deviceInfo),
                RequestInfo.normalizeIp(ipAddress)
        );

        passwordResetTokenRepository.save(resetToken);
        return rawCode;
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpiresTokens() {
        passwordResetTokenRepository.deleteInvalidTokens(Instant.now());
    }
}