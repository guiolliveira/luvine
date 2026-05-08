package com.javacore.spring_api_luvine.auth.domain.entity;

import com.javacore.spring_api_luvine.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, updatable = false)
    private String token;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(nullable = false)
    private String deviceInfo;

    @Column(nullable = false)
    private String ipAddress;

    private String replacedByToken;

    private RefreshToken(User user, String token, String deviceInfo, String ipAddress) {
        this.user = user;
        this.token = token;
        this.createdAt = Instant.now();
        this.expiresAt = this.createdAt.plus(7, ChronoUnit.DAYS);
        this.revoked = false;
        this.deviceInfo = deviceInfo != null ? deviceInfo : "unknown";
        this.ipAddress = ipAddress != null ? ipAddress : "unknown";
    }

    public static RefreshToken create(User user, String token, String deviceInfo, String ipAddress) {
        return new RefreshToken(user, token, deviceInfo, ipAddress);
    }

    public void revoke() {
        this.revoked = true;
    }

    public void markAsReplacedBy(String newToken) {
        this.replacedByToken = newToken;
    }
}