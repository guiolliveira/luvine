package com.javacore.spring_api_luvine.auth.application.UseCase;

import com.javacore.spring_api_luvine.auth.application.dto.LoginResponse;
import com.javacore.spring_api_luvine.auth.application.dto.RefreshTokenRequest;
import com.javacore.spring_api_luvine.auth.application.service.TokenService;
import com.javacore.spring_api_luvine.auth.domain.entity.RefreshToken;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidTokenException;
import com.javacore.spring_api_luvine.auth.infrastructure.repository.RefreshTokenRepository;
import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.common.util.TokenHash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginResponse execute(RefreshTokenRequest request) {
        log.debug("event=token_refresh_attempt");

        String tokenHash = TokenHash.hash(request.refreshToken());

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
}