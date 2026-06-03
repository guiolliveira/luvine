package com.javacore.spring_api_luvine.auth.domain.entity;

import com.javacore.spring_api_luvine.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, updatable = false)
    private String token;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used;

    private boolean revoked;

    @Column(nullable = false)
    private String deviceInfo;

    @Column(nullable = false)
    private String ipAddress;

    private PasswordResetToken(User user, String token, String deviceInfo, String ipAddress) {
        this.user = user;
        this.token = token;
        this.expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);
        this.used = false;
        this.revoked = false;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
    }

    public static PasswordResetToken create(User user, String token, String deviceInfo, String ipAddress) {
        return new PasswordResetToken(user, token, deviceInfo, ipAddress);
    }

    public void markAsUsed() {
        this.used = true;
    }
}