package com.javacore.spring_api_luvine.auth.repository;

import com.javacore.spring_api_luvine.auth.domain.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {
    Optional<EmailVerification> findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("UPDATE EmailVerification e SET e.used = TRUE WHERE e.user.id = :userId AND e.used = FALSE")
    int markAllCodesUsedForUser(Long userId);

    @Modifying
    @Query("DELETE FROM EmailVerification e WHERE e.expiresAt < :now OR e.used = TRUE")
    void deleteExpiredCode(@Param("now")Instant now);
}