package com.javacore.spring_api_luvine.auth.repository;

import com.javacore.spring_api_luvine.auth.domain.entity.EmailVerification;
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
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


@DisplayName("EmailVerificationRepository")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmailVerificationRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    EmailVerificationRepository repository;

    User user;

    @BeforeEach
    void setUp() {
        user = em.persist(buildUser("user@example.com"));
        em.flush();
    }

    // --- HELPERS ---------------------------------------------------------------

    private User buildUser(String email) {
        return User.create(new Email(email), new Name("John"), new Name("Doe"), "hashed-password", UserProvider.LOCAL);
    }

    private EmailVerification persistValidCode(User owner) {
        EmailVerification ev = EmailVerification.create(owner, "CODE-" + System.nanoTime());
        em.persist(ev);
        em.flush();
        return ev;
    }

    private EmailVerification persistUsedCode(User owner) {
        EmailVerification ev = EmailVerification.create(owner, "USED-" + System.nanoTime());
        em.persist(ev);
        em.flush();
        ev.markEmailAsUsed();
        em.flush();
        return ev;
    }

    private EmailVerification persistExpiredCode(User owner) {
        EmailVerification ev = EmailVerification.create(owner, "EXP-" + System.nanoTime());
        em.persist(ev);
        em.flush();

        Instant pastExpiry = Instant.now().minus(1, ChronoUnit.HOURS);
        em.getEntityManager()
                .createNativeQuery("UPDATE email_verification_codes SET expires_at = ? WHERE id = ?")
                .setParameter(1, pastExpiry)
                .setParameter(2, ev.getId())
                .executeUpdate();
        em.flush();
        em.clear();

        return repository.findById(ev.getId()).orElseThrow();
    }

    // --- MARK ALL CODES USED FOR USER ---------------------------------------------------------------

    @Nested
    @DisplayName("markAllCodesUsedForUser()")
    class MarkAllCodesUsedForUser {

        @Test
        @DisplayName("deve marcar todos os códigos não usados do usuário como usado")
        void shouldMarkAllUnusedCodesAsUsed() {
            persistValidCode(user);
            persistValidCode(user);

            repository.markAllCodesUsedForUser(user.getId());
            em.clear();

            long stillUnused = repository.findAll().stream()
                    .filter(e -> e.getUser().getId().equals(user.getId()))
                    .filter(e -> !e.isUsed())
                    .count();

            assertThat(stillUnused).isZero();
        }

        @Test
        @DisplayName("deve marcar como usado somente os códigos do usuário alvo")
        void shouldOnlyAffectTargetUser() {
            User otherUser = em.persist(buildUser("other@example.com"));
            em.flush();

            EmailVerification otherCode = persistValidCode(otherUser);
            persistValidCode(user);

            repository.markAllCodesUsedForUser(user.getId());
            em.clear();

            EmailVerification reloaded = repository.findById(otherCode.getId()).orElseThrow();
            assertThat(reloaded.isUsed()).isFalse();
        }

        @Test
        @DisplayName("não deve alterar códigos que já estão marcados como usado")
        void shouldNotTouchAlreadyUsedCodes() {
            EmailVerification alreadyUsed = persistUsedCode(user);

            repository.markAllCodesUsedForUser(user.getId());
            em.clear();

            EmailVerification reloaded = repository.findById(alreadyUsed.getId()).orElseThrow();
            assertThat(reloaded.isUsed()).isTrue();
        }

        @Test
        @DisplayName("não deve lançar exceção quando usuário não tiver códigos pendentes")
        void shouldNotThrowWhenNoPendingCodes() {
            assertDoesNotThrow(() -> repository.markAllCodesUsedForUser(user.getId()));
        }
    }

    // --- DELETE EXPIRED CODE ---------------------------------------------------------------

    @Nested
    @DisplayName("deleteExpiredCode()")
    class DeleteExpiredCode {

        @Test
        @DisplayName("deve deletar código com expiresAt no passado")
        void shouldDeleteExpiredCodes() {
            EmailVerification expired = persistExpiredCode(user);

            repository.deleteExpiredCode(Instant.now());
            em.clear();

            assertThat(repository.findById(expired.getId())).isEmpty();
        }

        @Test
        @DisplayName("deve deletar código marcado como usado mesmo dentro do prazo")
        void shouldDeleteUsedCodes() {
            EmailVerification used = persistUsedCode(user);

            repository.deleteExpiredCode(Instant.now());
            em.clear();

            assertThat(repository.findById(used.getId())).isEmpty();
        }

        @Test
        @DisplayName("não deve deletar código válido e não utilizado")
        void shouldNotDeleteValidAndUnusedCode() {
            EmailVerification valid = persistValidCode(user);

            repository.deleteExpiredCode(Instant.now());
            em.clear();

            assertThat(repository.findById(valid.getId())).isPresent();
        }

        @Test
        @DisplayName("deve deletar expirados e usados mas preservar os válidos na mesma operação")
        void shouldDeleteExpiredAndUsedButPreserveValid() {
            EmailVerification expired = persistExpiredCode(user);
            EmailVerification used    = persistUsedCode(user);
            EmailVerification valid   = persistValidCode(user);

            repository.deleteExpiredCode(Instant.now());
            em.clear();

            assertThat(repository.findById(expired.getId())).isEmpty();
            assertThat(repository.findById(used.getId())).isEmpty();
            assertThat(repository.findById(valid.getId())).isPresent();
        }

        @Test
        @DisplayName("não deve afetar códigos de outro usuário que ainda são válidos")
        void shouldNotAffectOtherUsersValidCodes() {
            User otherUser = em.persist(buildUser("other@example.com"));
            em.flush();

            EmailVerification otherValid = persistValidCode(otherUser);
            persistExpiredCode(user);

            repository.deleteExpiredCode(Instant.now());
            em.clear();

            assertThat(repository.findById(otherValid.getId())).isPresent();
        }
    }
}