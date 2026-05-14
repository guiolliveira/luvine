package com.javacore.spring_api_luvine.auth.service;

import com.javacore.spring_api_luvine.auth.domain.entity.RefreshToken;
import com.javacore.spring_api_luvine.auth.repository.RefreshTokenRepository;
import com.javacore.spring_api_luvine.shared.util.RequestInfo;
import com.javacore.spring_api_luvine.shared.util.TokenHash;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public String generateAccessToken(User user) {
        log.debug("event=access_token_generate publicId={}", user.getPublicId());

        Instant now = Instant.now();

        List<String> authorities = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer("Api-Luvine")
                .subject(user.getEmail().value())
                .claim("publicId", user.getPublicId().toString())
                .claim("jti", UUID.randomUUID().toString())
                .claim("type", "access")
                .claim("authorities", authorities)
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(
                JwsHeader.with(() -> "RS256").build(), claimsSet
        );

        String token = jwtEncoder.encode(parameters).getTokenValue();
        log.debug("event=access_token_generated publicId={}", user.getPublicId());
        return token;
    }

    @Transactional
    public String generateRefreshToken(User user, String deviceInfo, String ipAddress) {
        log.debug("event=refresh_token_generate publicId={} ip={}", user.getPublicId(), ipAddress);

        String refreshToken = TokenHash.generateSecureToken();

        RefreshToken token = RefreshToken.create(
                user,
                TokenHash.hash(refreshToken),
                RequestInfo.truncateDeviceInfo(deviceInfo),
                RequestInfo.normalizeIp(ipAddress)
        );

        refreshTokenRepository.save(token);

        log.debug("event=refresh_token_generated publicId={} ip={}", user.getPublicId(), ipAddress);
        return refreshToken;
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpiresTokens() {
        log.info("event=cleanup_expired_refresh_tokens_started");
        refreshTokenRepository.deleteInvalidTokens(Instant.now());
        log.info("event=cleanup_expired_refresh_tokens_completed");
    }
}