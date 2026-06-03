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
@Table(name = "email_verification_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private String verificationCode;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used;

    private EmailVerification(User user, String verificationCode) {
        this.user = user;
        this.verificationCode = verificationCode;
        this.expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);
        this.used = false;
    }

    public static EmailVerification create(User user, String verificationCode) {
        return new EmailVerification(user, verificationCode);
    }

    public void markEmailAsUsed() {
        this.used = true;
    }
}