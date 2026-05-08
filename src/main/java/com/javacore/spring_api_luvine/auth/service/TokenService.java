package com.javacore.spring_api_luvine.auth.service;

import com.javacore.spring_api_luvine.auth.domain.entity.RefreshToken;
import com.javacore.spring_api_luvine.auth.repository.RefreshTokenRepository;
import com.javacore.spring_api_luvine.shared.util.TokenHash;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer("Api-Luvine")
                .subject(user.getEmail())
                .claim("publicId", user.getPublicId().toString())
                .claim("jti", UUID.randomUUID().toString())
                .claim("type", "access")
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MICROS))
                .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(
                JwsHeader.with(() -> "RS256").build(), claimsSet
        );
        return jwtEncoder.encode(parameters).getTokenValue();
    }

    @Transactional
    public String generateRefreshToken(User user, String deviceInfo, String ipAddress) {
        String refreshToken = generateSecureToken();

        String info = deviceInfo != null ? deviceInfo.substring(0, Math.min(deviceInfo.length(), 255)) : null;

        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        RefreshToken token = RefreshToken.create(
                user,
                TokenHash.hash(refreshToken),
                info,
                ipAddress
        );

        refreshTokenRepository.save(token);

        return refreshToken;
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpiresTokens() {
        refreshTokenRepository.deleteExpiresToken(Instant.now());
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[64];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().withoutPadding().encodeToString(bytes);
    }
}