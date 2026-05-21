package com.javacore.spring_api_luvine.auth.domain.entity;

import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Password;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("EmailVerification")
class EmailVerificationTest {

    // --- FIXTURE ---------------------------------------------------------------

    private static final String CODE = "ABC123";

    private User anyUser() {
        return User.create(new Email("user@luvine.com"), new PersonName("Ana"), new PersonName("Silva"),
                new Password("hash"), UserProvider.LOCAL);
    }

    private EmailVerification newVerification() {
        return EmailVerification.create(anyUser(), CODE);
    }

    // --- EMAILVERIFICATION.CREATE() - FACTORY ---------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve associar o usuário recebido por parâmetro")
        void shouldAssociateUser() {
            User user = anyUser();
            EmailVerification ev = EmailVerification.create(user, CODE);

            assertThat(ev.getUser()).isSameAs(user);
        }

        @Test
        @DisplayName("deve armazenar o código de verificação recebido")
        void shouldStoreVerificationCode() {
            assertThat(newVerification().getVerificationCode()).isEqualTo(CODE);
        }

        @Test
        @DisplayName("deve iniciar com used = false")
        void shouldStartAsNotUsed() {
            assertThat(newVerification().isUsed()).isFalse();
        }

        @Test
        @DisplayName("id deve ser nulo antes de persistência (sem banco)")
        void shouldHaveNullIdBeforePersistence() {
            assertThat(newVerification().getId()).isNull();
        }

        @Test
        @DisplayName("deve registrar createdAt próximo ao instante atual")
        void shouldSetCreatedAtNearNow() {
            Instant before = Instant.now();
            EmailVerification ev = newVerification();
            Instant after = Instant.now();

            assertThat(ev.getCreatedAt())
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("deve definir expiresAt como createdAt + 15 minutos")
        void shouldSetExpiresAtFifteenMinutesAfterCreatedAt() {
            EmailVerification ev = newVerification();

            long diffMinutes = ChronoUnit.MINUTES.between(ev.getCreatedAt(), ev.getExpiresAt());

            assertThat(diffMinutes).isEqualTo(15);
        }

        @Test
        @DisplayName("expiresAt deve estar no futuro")
        void shouldSetExpiresAtInTheFuture() {
            assertThat(newVerification().getExpiresAt()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("instâncias distintas devem ter createdAt independentes")
        void shouldHaveIndependentTimestamps() throws InterruptedException {
            EmailVerification first = newVerification();
            Thread.sleep(5);
            EmailVerification second = newVerification();

            assertThat(second.getCreatedAt()).isAfter(first.getCreatedAt());
        }

        @Test
        @DisplayName("deve aceitar códigos de verificação diferentes por instância")
        void shouldAcceptDifferentCodes() {
            EmailVerification a = EmailVerification.create(anyUser(), "CODE-A");
            EmailVerification b = EmailVerification.create(anyUser(), "CODE-B");

            assertThat(a.getVerificationCode()).isEqualTo("CODE-A");
            assertThat(b.getVerificationCode()).isEqualTo("CODE-B");
        }
    }

    // --- MARK EMAIL AS USED ---------------------------------------------------------------

    @Nested
    @DisplayName("markEmailAsUsed()")
    class MarkEmailAsUsed {

        @Test
        @DisplayName("deve alterar used para true")
        void shouldSetUsedToTrue() {
            EmailVerification ev = newVerification();
            ev.markEmailAsUsed();

            assertThat(ev.isUsed()).isTrue();
        }

        @Test
        @DisplayName("deve ser idempotente: chamar duas vezes não causa erro")
        void shouldBeIdempotent() {
            EmailVerification ev = newVerification();
            ev.markEmailAsUsed();
            ev.markEmailAsUsed();

            assertThat(ev.isUsed()).isTrue();
        }

        @Test
        @DisplayName("não deve alterar verificationCode")
        void shouldNotChangeVerificationCode() {
            EmailVerification ev = newVerification();
            ev.markEmailAsUsed();

            assertThat(ev.getVerificationCode()).isEqualTo(CODE);
        }

        @Test
        @DisplayName("não deve alterar createdAt")
        void shouldNotChangeCreatedAt() {
            EmailVerification ev = newVerification();
            Instant createdAt = ev.getCreatedAt();
            ev.markEmailAsUsed();

            assertThat(ev.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("não deve alterar expiresAt")
        void shouldNotChangeExpiresAt() {
            EmailVerification ev = newVerification();
            Instant expiresAt = ev.getExpiresAt();
            ev.markEmailAsUsed();

            assertThat(ev.getExpiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("não deve alterar o usuário associado")
        void shouldNotChangeUser() {
            User user = anyUser();
            EmailVerification ev = EmailVerification.create(user, CODE);
            ev.markEmailAsUsed();

            assertThat(ev.getUser()).isSameAs(user);
        }
    }

    // --- VALIDADE DO CÓDIGO ---------------------------------------------------------------

    @Nested
    @DisplayName("Validade do código (expiresAt)")
    class CodeExpiry {

        @Test
        @DisplayName("código recém-criado deve estar dentro do prazo de validade")
        void freshCodeShouldBeWithinExpiry() {
            EmailVerification ev = newVerification();

            assertThat(ev.getExpiresAt()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("expiresAt deve ser exatamente 15 minutos depois de createdAt")
        void expiresAtShouldBeExactly15MinutesAfterCreatedAt() {
            EmailVerification ev = newVerification();

            Instant expectedExpiry = ev.getCreatedAt().plus(15, ChronoUnit.MINUTES);

            assertThat(ev.getExpiresAt())
                    .isCloseTo(expectedExpiry, within(1, ChronoUnit.SECONDS));
        }
    }

    // --- FLUXO DE NEGÓCIO ---------------------------------------------------------------

    @Nested
    @DisplayName("Fluxo de verificação de e-mail")
    class EmailVerificationFlow {

        @Test
        @DisplayName("fluxo completo: criar → verificar validade → usar")
        void fullFlow() {
            User user = anyUser();
            EmailVerification ev = EmailVerification.create(user, CODE);

            assertThat(ev.isUsed()).isFalse();
            assertThat(ev.getExpiresAt()).isAfter(Instant.now());
            assertThat(ev.getVerificationCode()).isEqualTo(CODE);
            assertThat(ev.getUser()).isSameAs(user);

            ev.markEmailAsUsed();

            assertThat(ev.isUsed()).isTrue();
            assertThat(ev.getVerificationCode()).isEqualTo(CODE);
            assertThat(ev.getExpiresAt()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("dois usuários distintos devem ter verificações independentes")
        void twoUsersShouldHaveIndependentVerifications() {
            User userA = User.create(new Email("a@exemple.com"), new PersonName("A"), new PersonName("A"),
                    new Password("h"), UserProvider.LOCAL);
            User userB = User.create(new Email("b@exemple.com"), new PersonName("B"), new PersonName("B"),
                    new Password("h"), UserProvider.LOCAL);

            EmailVerification evA = EmailVerification.create(userA, "CODE-A");
            EmailVerification evB = EmailVerification.create(userB, "CODE-B");

            evA.markEmailAsUsed();

            assertThat(evA.isUsed()).isTrue();
            assertThat(evB.isUsed()).isFalse();
            assertThat(evA.getUser()).isNotSameAs(evB.getUser());
            assertThat(evA.getVerificationCode()).isNotEqualTo(evB.getVerificationCode());
        }
    }
}