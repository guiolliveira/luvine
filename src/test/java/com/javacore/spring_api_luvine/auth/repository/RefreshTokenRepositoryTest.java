package com.javacore.spring_api_luvine.auth.repository;

import com.javacore.spring_api_luvine.auth.domain.entity.RefreshToken;
import com.javacore.spring_api_luvine.testcontainers.AbstractIntegrationTest;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("RefreshTokenRepository")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RefreshTokenRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    RefreshTokenRepository repository;

    User user;

    @BeforeEach
    void setUp() {
        user = em.persist(buildUser("user@example.com"));
        em.flush();
    }

    // --- HELPERS --------------------------------------------------------------

    private User buildUser(String email) {
        return User.create(email, "User", "Name", "hashed", UserProvider.LOCAL);
    }

    private RefreshToken persistToken(User owner, boolean revoked, Instant expiresAt) {
        RefreshToken token = RefreshToken.create(owner, UUID.randomUUID().toString(), "Brave/5.0", "127.0.0.1");
        em.persist(token);
        em.flush();

        if (revoked) {
            token.revoke();
            em.flush();
        }

        if (!expiresAt.equals(token.getExpiresAt())) {
            em.getEntityManager()
                    .createNativeQuery("UPDATE refresh_tokens SET expires_at = ? WHERE id = ?")
                    .setParameter(1, expiresAt)
                    .setParameter(2, token.getId())
                    .executeUpdate();
            em.flush();
            em.clear();
            return repository.findById(token.getId()).orElseThrow();
        }

        return token;
    }

    // --- deletedExpiresToken --------------------------------------------------

    @Nested
    @DisplayName("deletedExpiresToken()")
    class DeletedExpiresToken {

        @Test
        @DisplayName("deve deletar tokens expirados")
        void shouldDeleteExpiredTokens() {
            RefreshToken expired = persistToken(user, false, Instant.now().minusSeconds(60));

            repository.deleteExpiredToken(Instant.now());
            em.clear();

            assertThat(repository.findById(expired.getId())).isEmpty();
        }

        @Test
        @DisplayName("deve deletar tokens revogados independente da expiração")
        void shouldDeleteRevokedTokensRegardlessOfExpiry() {
            RefreshToken revoked = persistToken(user, true, Instant.now().plusSeconds(300));

            repository.deleteExpiredToken(Instant.now());
            em.clear();

            assertThat(repository.findById(revoked.getId())).isEmpty();
        }

        @Test
        @DisplayName("não deve deletar token válido e não revogado")
        void shouldNotDeleteValidAndNotRevokedToken() {
            RefreshToken valid = persistToken(user, false, Instant.now().plusSeconds(300));

            repository.deleteExpiredToken(Instant.now());
            em.clear();

            assertThat(repository.findById(valid.getId())).isPresent();
        }

        @Test
        @DisplayName("deve deletar expirados e revogados mas preservar os válidos na mesma operação")
        void shouldDeleteExpiredAndRevokedButPreserveValid() {
            RefreshToken expired = persistToken(user, false, Instant.now().minusSeconds(60));
            RefreshToken revoked = persistToken(user, true, Instant.now().plusSeconds(300));
            RefreshToken valid   = persistToken(user, false, Instant.now().plusSeconds(300));

            repository.deleteExpiredToken(Instant.now());
            em.clear();

            assertThat(repository.findById(expired.getId())).isEmpty();
            assertThat(repository.findById(revoked.getId())).isEmpty();
            assertThat(repository.findById(valid.getId())).isPresent();
        }

        @Test
        @DisplayName("não deve lançar exceção quando não houver tokens para deletar")
        void shouldNotThrowWhenNoTokensToDelete() {
            assertDoesNotThrow(() -> repository.deleteExpiredToken(Instant.now()));
        }
    }
}