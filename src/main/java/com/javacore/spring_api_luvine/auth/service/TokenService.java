package com.javacore.spring_api_luvine.auth.service;

import com.javacore.spring_api_luvine.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;

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
}