package com.javacore.spring_api_luvine.auth.domain.entity;

import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("RefreshToken")
class RefreshTokenTest {

    // --- FIXTURE -------------------------------------------------------------

    private static final String TOKEN       = "eyJhbGciOiJIUzI1NiJ9.token";
    private static final String DEVICE_INFO = "Mozilla/5.0 Chrome/120";
    private static final String IP_ADDRESS  = "192.168.0.1";

    private User anyUser() {
        return User.create("user@example.com", "User", "Name", "hash", UserProvider.LOCAL);
    }

    private RefreshToken newToken() {
        return RefreshToken.create(anyUser(), TOKEN, DEVICE_INFO, IP_ADDRESS);
    }

    // --- REFRESHTOKEN.CREATE() - FACTORY -------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve associar o usuário recebido por parâmetro")
        void shouldAssociateUser() {
            User user = anyUser();
            RefreshToken rt = RefreshToken.create(user, TOKEN, DEVICE_INFO, IP_ADDRESS);

            assertThat(rt.getUser()).isSameAs(user);
        }

        @Test
        @DisplayName("deve armazenar o token recebido")
        void shouldStoreToken() {
            assertThat(newToken().getToken()).isEqualTo(TOKEN);
        }

        @Test
        @DisplayName("deve armazenar deviceInfo recebido")
        void shouldStoreDeviceInfo() {
            assertThat(newToken().getDeviceInfo()).isEqualTo(DEVICE_INFO);
        }

        @Test
        @DisplayName("deve armazenar ipAddress recebido")
        void shouldStoreIpAddress() {
            assertThat(newToken().getIpAddress()).isEqualTo(IP_ADDRESS);
        }

        @Test
        @DisplayName("deve iniciar com revoked = false")
        void shouldStartAsNotRevoked() {
            assertThat(newToken().isRevoked()).isFalse();
        }

        @Test
        @DisplayName("deve iniciar replacedByToken como nulo")
        void shouldStartReplacedByTokenAsNull() {
            assertThat(newToken().getReplacedByToken()).isNull();
        }

        @Test
        @DisplayName("id deve ser nulo antes de persistência (sem banco)")
        void shouldHaveNullIdBeforePersistence() {
            assertThat(newToken().getId()).isNull();
        }

        @Test
        @DisplayName("deve registrar createdAt próximo ao instante atual")
        void shouldSetCreatedAtNearNow() {
            Instant before = Instant.now();
            RefreshToken rt = newToken();
            Instant after = Instant.now();

            assertThat(rt.getCreatedAt())
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("deve definir expiresAt como createdAt + 7 dias")
        void shouldSetExpiresAtSevenDaysAfterCreatedAt() {
            RefreshToken rt = newToken();

            long diffDays = ChronoUnit.DAYS.between(rt.getCreatedAt(), rt.getExpiresAt());

            assertThat(diffDays).isEqualTo(7);
        }

        @Test
        @DisplayName("expiresAt deve ser exatamente 7 dias depois de createdAt")
        void shouldSetExpiresAtExactlySevenDays() {
            RefreshToken rt = newToken();

            Instant expected = rt.getCreatedAt().plus(7, ChronoUnit.DAYS);

            assertThat(rt.getExpiresAt())
                    .isCloseTo(expected, within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("expiresAt deve estar no futuro")
        void shouldSetExpiresAtInTheFuture() {
            assertThat(newToken().getExpiresAt()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("deviceInfo nulo deve ser substituído por 'unknown'")
        void shouldFallbackDeviceInfoWhenNull() {
            RefreshToken rt = RefreshToken.create(anyUser(), TOKEN, null, IP_ADDRESS);

            assertThat(rt.getDeviceInfo()).isEqualTo("unknown");
        }

        @Test
        @DisplayName("ipAddress nulo deve ser substituído por 'unknown'")
        void shouldFallbackIpAddressWhenNull() {
            RefreshToken rt = RefreshToken.create(anyUser(), TOKEN, DEVICE_INFO, null);

            assertThat(rt.getIpAddress()).isEqualTo("unknown");
        }

        @Test
        @DisplayName("ambos nulos devem ser substituídos por 'unknown'")
        void shouldFallbackBothWhenNull() {
            RefreshToken rt = RefreshToken.create(anyUser(), TOKEN, null, null);

            assertThat(rt.getDeviceInfo()).isEqualTo("unknown");
            assertThat(rt.getIpAddress()).isEqualTo("unknown");
        }

        @Test
        @DisplayName("instâncias distintas devem ter createdAt independentes")
        void shouldHaveIndependentTimestamps() throws InterruptedException {
            RefreshToken first = newToken();
            Thread.sleep(5);
            RefreshToken second = newToken();

            assertThat(second.getCreatedAt()).isAfter(first.getCreatedAt());
        }
    }

    // --- REVOKE -------------------------------------------------------------

    @Nested
    @DisplayName("revoke()")
    class Revoke {

        @Test
        @DisplayName("deve alterar revoked para true")
        void shouldSetRevokedToTrue() {
            RefreshToken rt = newToken();
            rt.revoke();

            assertThat(rt.isRevoked()).isTrue();
        }

        @Test
        @DisplayName("deve ser idempotente: revogar duas vezes não causa erro")
        void shouldBeIdempotent() {
            RefreshToken rt = newToken();
            rt.revoke();
            rt.revoke();

            assertThat(rt.isRevoked()).isTrue();
        }

        @Test
        @DisplayName("não deve alterar token")
        void shouldNotChangeToken() {
            RefreshToken rt = newToken();
            rt.revoke();

            assertThat(rt.getToken()).isEqualTo(TOKEN);
        }

        @Test
        @DisplayName("não deve alterar createdAt")
        void shouldNotChangeCreatedAt() {
            RefreshToken rt = newToken();
            Instant createdAt = rt.getCreatedAt();
            rt.revoke();

            assertThat(rt.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("não deve alterar expiresAt")
        void shouldNotChangeExpiresAt() {
            RefreshToken rt = newToken();
            Instant expiresAt = rt.getExpiresAt();
            rt.revoke();

            assertThat(rt.getExpiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("não deve alterar replacedByToken")
        void shouldNotChangeReplacedByToken() {
            RefreshToken rt = newToken();
            rt.revoke();

            assertThat(rt.getReplacedByToken()).isNull();
        }

        @Test
        @DisplayName("não deve alterar deviceInfo nem ipAddress")
        void shouldNotChangeDeviceInfoOrIpAddress() {
            RefreshToken rt = newToken();
            rt.revoke();

            assertThat(rt.getDeviceInfo()).isEqualTo(DEVICE_INFO);
            assertThat(rt.getIpAddress()).isEqualTo(IP_ADDRESS);
        }
    }

    // --- MARK AS REPLACED BY -------------------------------------------------------------

    @Nested
    @DisplayName("markAsReplacedBy()")
    class MarkAsReplacedBy {

        @Test
        @DisplayName("deve armazenar o token substituto")
        void shouldStoreReplacementToken() {
            RefreshToken rt = newToken();
            rt.markAsReplacedBy("new-token-xyz");

            assertThat(rt.getReplacedByToken()).isEqualTo("new-token-xyz");
        }

        @Test
        @DisplayName("deve sobrescrever o valor ao ser chamado novamente")
        void shouldOverwriteOnSecondCall() {
            RefreshToken rt = newToken();
            rt.markAsReplacedBy("first-replacement");
            rt.markAsReplacedBy("second-replacement");

            assertThat(rt.getReplacedByToken()).isEqualTo("second-replacement");
        }

        @Test
        @DisplayName("deve aceitar null como valor de replacedByToken")
        void shouldAcceptNullReplacement() {
            RefreshToken rt = newToken();
            rt.markAsReplacedBy(null);

            assertThat(rt.getReplacedByToken()).isNull();
        }

        @Test
        @DisplayName("não deve alterar revoked")
        void shouldNotChangeRevoked() {
            RefreshToken rt = newToken();
            rt.markAsReplacedBy("new-token");

            assertThat(rt.isRevoked()).isFalse();
        }

        @Test
        @DisplayName("não deve alterar token original")
        void shouldNotChangeOriginalToken() {
            RefreshToken rt = newToken();
            rt.markAsReplacedBy("new-token");

            assertThat(rt.getToken()).isEqualTo(TOKEN);
        }

        @Test
        @DisplayName("não deve alterar createdAt nem expiresAt")
        void shouldNotChangeTimestamps() {
            RefreshToken rt = newToken();
            Instant createdAt = rt.getCreatedAt();
            Instant expiresAt = rt.getExpiresAt();

            rt.markAsReplacedBy("new-token");

            assertThat(rt.getCreatedAt()).isEqualTo(createdAt);
            assertThat(rt.getExpiresAt()).isEqualTo(expiresAt);
        }
    }

    // --- FLUXO DE NEGÓCIO - CENÁRIOS INTEGRADOS -------------------------------------------------------------

    @Nested
    @DisplayName("Fluxo de rotação de token (token rotation)")
    class TokenRotationFlow {

        @Test
        @DisplayName("fluxo completo: emitir → substituir → revogar")
        void fullRotationFlow() {
            User user = anyUser();
            RefreshToken old = RefreshToken.create(user, "token-v1", DEVICE_INFO, IP_ADDRESS);

            assertThat(old.isRevoked()).isFalse();
            assertThat(old.getReplacedByToken()).isNull();

            String newTokenValue = "token-v2";
            old.markAsReplacedBy(newTokenValue);
            old.revoke();

            assertThat(old.isRevoked()).isTrue();
            assertThat(old.getReplacedByToken()).isEqualTo(newTokenValue);
            assertThat(old.getToken()).isEqualTo("token-v1");
        }

        @Test
        @DisplayName("revogar sem marcar substituto deve funcionar (revogação por logout)")
        void revokeWithoutReplacementShouldWork() {
            RefreshToken rt = newToken();
            rt.revoke();

            assertThat(rt.isRevoked()).isTrue();
            assertThat(rt.getReplacedByToken()).isNull();
        }

        @Test
        @DisplayName("tokens de usuários distintos são independentes entre si")
        void tokensShouldBeIndependentPerUser() {
            User userA = User.create("a@example.com", "A", "A", "h", UserProvider.LOCAL);
            User userB = User.create("b@example.com", "B", "B", "h", UserProvider.LOCAL);

            RefreshToken rtA = RefreshToken.create(userA, "token-a", DEVICE_INFO, IP_ADDRESS);
            RefreshToken rtB = RefreshToken.create(userB, "token-b", DEVICE_INFO, IP_ADDRESS);

            rtA.revoke();

            assertThat(rtA.isRevoked()).isTrue();
            assertThat(rtB.isRevoked()).isFalse();
        }

        @Test
        @DisplayName("mesmo usuário pode ter múltiplos tokens simultâneos independentes")
        void sameUserCanHaveMultipleIndependentTokens() {
            User user = anyUser();

            RefreshToken mobile  = RefreshToken.create(user, "token-mobile",  "Mobile Safari", "10.0.0.1");
            RefreshToken desktop = RefreshToken.create(user, "token-desktop", "Chrome/120",     "10.0.0.2");

            mobile.revoke();

            assertThat(mobile.isRevoked()).isTrue();
            assertThat(desktop.isRevoked()).isFalse();
            assertThat(mobile.getToken()).isNotEqualTo(desktop.getToken());
            assertThat(mobile.getDeviceInfo()).isNotEqualTo(desktop.getDeviceInfo());
        }
    }
}