package com.javacore.spring_api_luvine.auth.service;

import com.javacore.spring_api_luvine.auth.domain.entity.PasswordResetToken;
import com.javacore.spring_api_luvine.auth.repository.PasswordResetTokenRepository;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("PasswordResetTokenService")
@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceTest {

    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private PasswordResetTokenService passwordResetTokenService;

    // --- HELPERS ------------------------------------------------------------------

    private static final String DEVICE_INFO = "Mozilla/5.0";
    private static final String IP_ADDRESS  = "192.168.0.1";

    private User buildUser() {
        return User.create(
                new Email("user@example.com"),
                new Name("João"),
                new Name("Silva"),
                "hashed-password",
                UserProvider.LOCAL
        );
    }

    // --- GENERATE PASSWORD RESET TOKEN ------------------------------------------------------------------

    @Nested
    @DisplayName("generatePasswordResetToken()")
    class GeneratePasswordResetToken {

        @Test
        @DisplayName("deve retornar um rawToken não nulo e não vazio")
        void generatePasswordResetToken_validArgs_returnsNonBlankRawToken() {
            User user = buildUser();

            String rawToken = passwordResetTokenService.generatePasswordResetToken(user, DEVICE_INFO, IP_ADDRESS);

            assertThat(rawToken).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("deve persistir o PasswordResetToken no repositório")
        void generatePasswordResetToken_validArgs_savesToken() {
            User user = buildUser();

            passwordResetTokenService.generatePasswordResetToken(user, DEVICE_INFO, IP_ADDRESS);

            then(passwordResetTokenRepository).should(times(1)).save(any(PasswordResetToken.class));
        }

        @Test
        @DisplayName("deve salvar token com hash diferente do rawToken retornado")
        void generatePasswordResetToken_validArgs_savesHashedToken() {
            User user = buildUser();
            ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);

            String rawToken = passwordResetTokenService.generatePasswordResetToken(user, DEVICE_INFO, IP_ADDRESS);

            then(passwordResetTokenRepository).should().save(captor.capture());
            assertThat(captor.getValue().getToken()).isNotEqualTo(rawToken);
        }

        @Test
        @DisplayName("deve gerar rawTokens distintos a cada chamada")
        void generatePasswordResetToken_calledTwice_returnsDifferentTokens() {
            User user = buildUser();

            String first  = passwordResetTokenService.generatePasswordResetToken(user, DEVICE_INFO, IP_ADDRESS);
            String second = passwordResetTokenService.generatePasswordResetToken(user, DEVICE_INFO, IP_ADDRESS);

            assertThat(first).isNotEqualTo(second);
        }

        @Test
        @DisplayName("deve salvar token associado ao usuário correto")
        void generatePasswordResetToken_validArgs_savesTokenWithCorrectUser() {
            User user = buildUser();
            ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);

            passwordResetTokenService.generatePasswordResetToken(user, DEVICE_INFO, IP_ADDRESS);

            then(passwordResetTokenRepository).should().save(captor.capture());
            assertThat(captor.getValue().getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("deve aceitar deviceInfo e ipAddress nulos sem lançar exceção")
        void generatePasswordResetToken_nullDeviceAndIp_doesNotThrow() {
            User user = buildUser();

            assertThatNoException()
                    .isThrownBy(() -> passwordResetTokenService.generatePasswordResetToken(user, null, null));
        }
    }

    // --- CLEAN EXPIRED TOKENS ------------------------------------------------------------------

    @Nested
    @DisplayName("cleanExpiresTokens()")
    class CleanExpiredTokens {

        @Test
        @DisplayName("deve chamar deleteInvalidTokens exatamente uma vez")
        void cleanExpiredTokens_callsDeleteInvalidTokensOnce() {
            passwordResetTokenService.cleanExpiresTokens();

            then(passwordResetTokenRepository).should(times(1)).deleteInvalidTokens(any(Instant.class));
        }

        @Test
        @DisplayName("deve passar um Instant próximo ao momento atual")
        void cleanExpiredTokens_passesInstantNearNow() {
            ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
            Instant before = Instant.now();

            passwordResetTokenService.cleanExpiresTokens();

            Instant after = Instant.now();
            then(passwordResetTokenRepository).should().deleteInvalidTokens(captor.capture());
            assertThat(captor.getValue())
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("deve completar sem lançar exceção")
        void cleanExpiredTokens_doesNotThrow() {
            assertThatNoException()
                    .isThrownBy(() -> passwordResetTokenService.cleanExpiresTokens());
        }
    }
}