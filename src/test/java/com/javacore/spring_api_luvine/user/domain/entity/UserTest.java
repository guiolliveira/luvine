package com.javacore.spring_api_luvine.user.domain.entity;

import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User")
class UserTest {

    // --- FIXTURE -------------------------------------------------------------

    private static final String EMAIL = "user@example.com";
    private static final String FIRST_NAME = "user";
    private static final String LAST_NAME = "name";
    private static final String PASSWORD = "hashed-password";

    private User localUser() {
        return User.create(new Email(EMAIL), new PersonName(FIRST_NAME), new PersonName(LAST_NAME), PASSWORD, UserProvider.LOCAL);
    }

    private User googleUser() {
        return User.create(new Email(EMAIL), new PersonName(FIRST_NAME), new PersonName(LAST_NAME), PASSWORD, UserProvider.GOOGLE);
    }

    // --- USER.CREATE() - FACTORY -------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve persistir email, firstName, lastName e password corretamente")
        void shouldPersistAllScalarFields() {
            User user = localUser();

            assertThat(user.getEmail().value()).isEqualTo(EMAIL);
            assertThat(user.getFirstName().value()).isEqualTo(new PersonName(FIRST_NAME).value());
            assertThat(user.getLastName().value()).isEqualTo(new PersonName(LAST_NAME).value());
            assertThat(user.getPassword()).isEqualTo(PASSWORD);
        }

        @Test
        @DisplayName("deve gerar um publicId UUID não nulo")
        void shouldGenerateNonNullPublicId() {
            assertThat(localUser().getPublicId()).isNotNull();
        }

        @Test
        @DisplayName("cada instância deve ter um publicId único")
        void shouldGenerateDistinctPublicIds() {
            User a = localUser();
            User b = localUser();

            assertThat(a.getPublicId()).isNotEqualTo(b.getPublicId());
        }

        @Test
        @DisplayName("deve definir o userProvider recebido por parâmetro")
        void shouldSetUserProvider() {
            assertThat(localUser().getUserProvider()).isEqualTo(UserProvider.LOCAL);
            assertThat(googleUser().getUserProvider()).isEqualTo(UserProvider.GOOGLE);
        }

        @Test
        @DisplayName("deve iniciar com active = true")
        void shouldStartActive() {
            assertThat(localUser().isActive()).isTrue();
        }

        @Test
        @DisplayName("deve iniciar com emailVerified = false")
        void shouldStartWithEmailNotVerified() {
            assertThat(localUser().isEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("deve iniciar verificationEmailRequestCount com zero")
        void shouldStartVerificationCountAtZero() {
            assertThat(localUser().getVerificationEmailRequestCount()).isZero();
        }

        @Test
        @DisplayName("deve iniciar lastVerificationEmailSentAt como nulo")
        void shouldStartLastVerificationEmailSentAtAsNull() {
            assertThat(localUser().getLastVerificationEmailSentAt()).isNull();
        }

        @Test
        @DisplayName("deve registrar createdAt próximo ao instante atual")
        void shouldSetCreatedAtNearNow() {
            Instant before = Instant.now();
            User user = localUser();
            Instant after = Instant.now();

            assertThat(user.getCreatedAt())
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("deve registrar updatedAt próximo ao instante atual")
        void shouldSetUpdatedAtNearNow() {
            Instant before = Instant.now();
            User user = localUser();
            Instant after = Instant.now();

            assertThat(user.getUpdatedAt())
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("id deve ser nulo antes de persistência (sem banco)")
        void shouldHaveNullIdBeforePersistence() {
            assertThat(localUser().getId()).isNull();
        }
    }

    // --- MARK EMAIL AS VERIFIED -------------------------------------------------------------

    @Nested
    @DisplayName("markEmailAsVerified()")
    class MarkEmailAsVerified {

        @Test
        @DisplayName("deve alterar emailVerified para true")
        void shouldSetEmailVerifiedToTrue() {
            User user = localUser();
            user.markEmailAsVerified();

            assertThat(user.isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("deve ser idempotente: chamar duas vezes não causa erro")
        void shouldBeIdempotent() {
            User user = localUser();
            user.markEmailAsVerified();
            user.markEmailAsVerified();

            assertThat(user.isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("não deve alterar outros campos do usuário")
        void shouldNotAffectOtherFields() {
            User user = localUser();
            Email emailBefore = user.getEmail();
            boolean activeBefore = user.isActive();

            user.markEmailAsVerified();

            assertThat(user.getEmail()).isEqualTo(emailBefore);
            assertThat(user.isActive()).isEqualTo(activeBefore);
            assertThat(user.getVerificationEmailRequestCount()).isZero();
        }
    }

    // --- MARK VERIFICATION EMAIL SENT -------------------------------------------------------------

    @Nested
    @DisplayName("markVerificationEmailSent()")
    class MarkVerificationEmailSent {

        @Test
        @DisplayName("deve incrementar verificationEmailRequestCount em 1")
        void shouldIncrementVerificationCount() {
            User user = localUser();
            user.markVerificationEmailSent();

            assertThat(user.getVerificationEmailRequestCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("deve acumular contagem em chamadas sucessivas")
        void shouldAccumulateCountOnMultipleCalls() {
            User user = localUser();
            user.markVerificationEmailSent();
            user.markVerificationEmailSent();
            user.markVerificationEmailSent();

            assertThat(user.getVerificationEmailRequestCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("deve atualizar lastVerificationEmailSentAt para próximo ao instante atual")
        void shouldUpdateLastVerificationEmailSentAt() {
            User user = localUser();
            Instant before = Instant.now();
            user.markVerificationEmailSent();
            Instant after = Instant.now();

            assertThat(user.getLastVerificationEmailSentAt())
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("cada chamada deve atualizar lastVerificationEmailSentAt")
        void shouldRefreshTimestampOnEachCall() throws InterruptedException {
            User user = localUser();
            user.markVerificationEmailSent();
            Instant first = user.getLastVerificationEmailSentAt();

            Thread.sleep(5);
            user.markVerificationEmailSent();
            Instant second = user.getLastVerificationEmailSentAt();

            assertThat(second).isAfter(first);
        }

        @Test
        @DisplayName("não deve alterar emailVerified")
        void shouldNotAffectEmailVerified() {
            User user = localUser();
            user.markVerificationEmailSent();

            assertThat(user.isEmailVerified()).isFalse();
        }
    }

    // --- RESET EMAIL VERIFICATION REQUEST -------------------------------------------------------------

    @Nested
    @DisplayName("resetEmailVerificationRequests()")
    class ResetEmailVerificationRequests {

        @Test
        @DisplayName("deve zerar verificationEmailRequestCount")
        void shouldResetCountToZero() {
            User user = localUser();
            user.markVerificationEmailSent();
            user.markVerificationEmailSent();

            user.resetEmailVerificationRequests();

            assertThat(user.getVerificationEmailRequestCount()).isZero();
        }

        @Test
        @DisplayName("deve ser idempotente: zerar novamente não causa erro")
        void shouldBeIdempotent() {
            User user = localUser();
            user.resetEmailVerificationRequests();
            user.resetEmailVerificationRequests();

            assertThat(user.getVerificationEmailRequestCount()).isZero();
        }

        @Test
        @DisplayName("não deve alterar lastVerificationEmailSentAt")
        void shouldNotClearLastVerificationEmailSentAt() {
            User user = localUser();
            user.markVerificationEmailSent();
            Instant sentAt = user.getLastVerificationEmailSentAt();

            user.resetEmailVerificationRequests();

            assertThat(user.getLastVerificationEmailSentAt()).isEqualTo(sentAt);
        }

        @Test
        @DisplayName("não deve alterar emailVerified nem active")
        void shouldNotAffectOtherFlags() {
            User user = localUser();
            user.markEmailAsVerified();
            user.markVerificationEmailSent();

            user.resetEmailVerificationRequests();

            assertThat(user.isEmailVerified()).isTrue();
            assertThat(user.isActive()).isTrue();
        }
    }

    // --- USERDETAILS - CONTRATO DE SEGURANÇA -------------------------------------------------------------

    @Nested
    @DisplayName("UserDetails (contrato Spring Security)")
    class UserDetailsContract {

        @Test
        @DisplayName("getUsername() deve retornar o email do usuário")
        void getUsernameShouldReturnEmail() {
            assertThat(localUser().getUsername()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("getAuthorities() deve retornar as authorities do role do usuário")
        void getAuthoritiesShouldReturnUserRoleAuthorities() {
            User user = localUser();

            assertThat(user.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_CUSTOMER");
        }

        @Test
        @DisplayName("isEnabled() deve refletir o valor de isActive()")
        void isEnabledShouldReflectIsActive() {
            User user = localUser();

            assertThat(user.isEnabled()).isTrue();
            assertThat(user.isEnabled()).isEqualTo(user.isActive());
        }

        @Test
        @DisplayName("isAccountNonExpired() deve retornar true por padrão")
        void isAccountNonExpiredShouldReturnTrue() {
            assertThat(localUser().isAccountNonExpired()).isTrue();
        }

        @Test
        @DisplayName("isAccountNonLocked() deve retornar true por padrão")
        void isAccountNonLockedShouldReturnTrue() {
            assertThat(localUser().isAccountNonLocked()).isTrue();
        }

        @Test
        @DisplayName("isCredentialsNonExpired() deve retornar true por padrão")
        void isCredentialsNonExpiredShouldReturnTrue() {
            assertThat(localUser().isCredentialsNonExpired()).isTrue();
        }
    }

    // --- FLUXO DE NEGÓCIO - CENÁRIOS INTEGRADOS -------------------------------------------------------------

    @Nested
    @DisplayName("Fluxo de verificação de e-mail")
    class EmailVerificationFlow {

        @Test
        @DisplayName("fluxo completo: envio → verificação → reset")
        void fullVerificationFlow() {
            User user = localUser();

            user.markVerificationEmailSent();
            assertThat(user.getVerificationEmailRequestCount()).isEqualTo(1);
            assertThat(user.getLastVerificationEmailSentAt()).isNotNull();
            assertThat(user.isEmailVerified()).isFalse();

            user.markEmailAsVerified();
            assertThat(user.isEmailVerified()).isTrue();

            user.resetEmailVerificationRequests();
            assertThat(user.getVerificationEmailRequestCount()).isZero();
            assertThat(user.isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("múltiplos envios acumulam corretamente o contador")
        void multipleEmailsSentShouldAccumulateCount() {
            User user = localUser();

            for (int i = 1; i <= 5; i++) {
                user.markVerificationEmailSent();
                assertThat(user.getVerificationEmailRequestCount()).isEqualTo(i);
            }
        }

        @Test
        @DisplayName("reset após múltiplos envios devolve contador a zero")
        void resetAfterMultipleSendsShouldReturnToZero() {
            User user = localUser();
            user.markVerificationEmailSent();
            user.markVerificationEmailSent();
            user.markVerificationEmailSent();

            user.resetEmailVerificationRequests();

            assertThat(user.getVerificationEmailRequestCount()).isZero();
        }
    }
}