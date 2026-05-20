package com.javacore.spring_api_luvine.auth.repository;

import com.javacore.spring_api_luvine.auth.domain.entity.PasswordResetToken;
import com.javacore.spring_api_luvine.auth.infrastructure.repository.PasswordResetTokenRepository;
import com.javacore.spring_api_luvine.testcontainers.AbstractIntegrationTest;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Name;
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

@DisplayName("PasswordResetTokenRepository")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PasswordResetTokenRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    PasswordResetTokenRepository repository;

    User user;

    @BeforeEach
    void setUp() {
        user = em.persist(buildUser("user@example.com"));
        em.flush();
    }

    // --- HELPERS --------------------------------------------------------------

    private User buildUser(String email) {
        return User.create(new Email(email), new Name("User"), new Name("Name"), "hashed", UserProvider.LOCAL);
    }

    private PasswordResetToken persistToken(User owner, boolean used, Instant expiresAt) {
        PasswordResetToken token = PasswordResetToken.create(
                owner,
                UUID.randomUUID().toString(),
                "Mozilla/5.0",
                "127.0.0.1"
        );
        em.persist(token);
        em.flush();

        if (!expiresAt.equals(token.getExpiresAt())) {
            em.getEntityManager()
                    .createNativeQuery("UPDATE password_reset_tokens SET expires_at = ? WHERE id = ?")
                    .setParameter(1, expiresAt)
                    .setParameter(2, token.getId())
                    .executeUpdate();
            em.flush();
            em.clear();
            token = repository.findById(token.getId()).orElseThrow();
        }

        if (used) {
            token.markAsUsed();
            em.flush();
        }

        return token;
    }

    // --- revokeAllUserTokens --------------------------------------------------

    @Nested
    @DisplayName("revokeAllUserTokens()")
    class RevokeAllUserTokens {

        @Test
        @DisplayName("deve revogar todos os tokens ativos do usuário")
        void shouldRevokeAllActiveTokensOfUser() {
            PasswordResetToken token1 = persistToken(user, false, Instant.now().plusSeconds(300));
            PasswordResetToken token2 = persistToken(user, false, Instant.now().plusSeconds(300));

            repository.revokeAllUserTokens(user);
            em.clear();

            assertThat(repository.findById(token1.getId()).orElseThrow().isRevoked()).isTrue();
            assertThat(repository.findById(token2.getId()).orElseThrow().isRevoked()).isTrue();
        }

        @Test
        @DisplayName("não deve afetar tokens já utilizados")
        void shouldNotAffectAlreadyUsedTokens() {
            PasswordResetToken used = persistToken(user, true, Instant.now().plusSeconds(300));

            repository.revokeAllUserTokens(user);
            em.clear();

            assertThat(repository.findById(used.getId()).orElseThrow().isRevoked()).isFalse();
        }

        @Test
        @DisplayName("não deve revogar tokens de outro usuário")
        void shouldNotRevokeTokensOfAnotherUser() {
            User otherUser = em.persist(buildUser("other@example.com"));
            em.flush();

            PasswordResetToken otherToken = persistToken(otherUser, false, Instant.now().plusSeconds(300));

            repository.revokeAllUserTokens(user);
            em.clear();

            assertThat(repository.findById(otherToken.getId()).orElseThrow().isRevoked()).isFalse();
        }

        @Test
        @DisplayName("não deve lançar exceção quando o usuário não possuir tokens ativos")
        void shouldNotThrowWhenUserHasNoActiveTokens() {
            assertDoesNotThrow(() -> repository.revokeAllUserTokens(user));
        }

        @Test
        @DisplayName("deve revogar apenas os tokens ativos, preservando os já utilizados intocados")
        void shouldRevokeOnlyActiveTokensAndLeaveUsedUntouched() {
            PasswordResetToken active = persistToken(user, false, Instant.now().plusSeconds(300));
            PasswordResetToken used   = persistToken(user, true,  Instant.now().plusSeconds(300));

            repository.revokeAllUserTokens(user);
            em.clear();

            assertThat(repository.findById(active.getId()).orElseThrow().isRevoked()).isTrue();
            assertThat(repository.findById(used.getId()).orElseThrow().isRevoked()).isFalse();
        }
    }

    // --- deleteInvalidTokens --------------------------------------------------

    @Nested
    @DisplayName("deleteInvalidTokens()")
    class DeleteInvalidTokens {

        @Test
        @DisplayName("deve deletar tokens expirados")
        void shouldDeleteExpiredTokens() {
            PasswordResetToken expired = persistToken(user, false, Instant.now().minusSeconds(60));

            repository.deleteInvalidTokens(Instant.now());
            em.clear();

            assertThat(repository.findById(expired.getId())).isEmpty();
        }

        @Test
        @DisplayName("deve deletar tokens já utilizados independente da expiração")
        void shouldDeleteUsedTokensRegardlessOfExpiry() {
            PasswordResetToken used = persistToken(user, true, Instant.now().plusSeconds(300));

            repository.deleteInvalidTokens(Instant.now());
            em.clear();

            assertThat(repository.findById(used.getId())).isEmpty();
        }

        @Test
        @DisplayName("não deve deletar token válido e não utilizado")
        void shouldNotDeleteValidAndNotUsedToken() {
            PasswordResetToken valid = persistToken(user, false, Instant.now().plusSeconds(300));

            repository.deleteInvalidTokens(Instant.now());
            em.clear();

            assertThat(repository.findById(valid.getId())).isPresent();
        }

        @Test
        @DisplayName("deve deletar expirados e utilizados mas preservar os válidos na mesma operação")
        void shouldDeleteExpiredAndUsedButPreserveValid() {
            PasswordResetToken expired = persistToken(user, false, Instant.now().minusSeconds(60));
            PasswordResetToken used    = persistToken(user, true,  Instant.now().plusSeconds(300));
            PasswordResetToken valid   = persistToken(user, false, Instant.now().plusSeconds(300));

            repository.deleteInvalidTokens(Instant.now());
            em.clear();

            assertThat(repository.findById(expired.getId())).isEmpty();
            assertThat(repository.findById(used.getId())).isEmpty();
            assertThat(repository.findById(valid.getId())).isPresent();
        }

        @Test
        @DisplayName("não deve lançar exceção quando não houver tokens para deletar")
        void shouldNotThrowWhenNoTokensToDelete() {
            assertDoesNotThrow(() -> repository.deleteInvalidTokens(Instant.now()));
        }
    }
}