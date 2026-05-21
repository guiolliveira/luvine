package com.javacore.spring_api_luvine.auth.application.usecase;

import com.javacore.spring_api_luvine.auth.application.dto.ForgotPasswordRequest;
import com.javacore.spring_api_luvine.auth.application.service.PasswordResetTokenService;
import com.javacore.spring_api_luvine.auth.domain.exception.EmailNotVerifiedException;
import com.javacore.spring_api_luvine.auth.infrastructure.repository.PasswordResetTokenRepository;
import com.javacore.spring_api_luvine.common.messaging.dto.EmailMessageRequest;
import com.javacore.spring_api_luvine.common.messaging.service.producer.ProducerService;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Password;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("ForgotPasswordUseCase")
@ExtendWith(MockitoExtension.class)
class ForgotPasswordUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private ProducerService producerService;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordResetTokenService passwordResetTokenService;

    @InjectMocks
    private ForgotPasswordUseCase forgotPasswordUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final String VALID_EMAIL = "user@example.com";
    private static final String DEVICE_INFO = "Mozilla/5.0";
    private static final String IP_ADDRESS = "192.168.0.1";

    private ForgotPasswordRequest validRequest() {
        return new ForgotPasswordRequest(VALID_EMAIL);
    }

    private User buildVerifiedUser() {
        User user = User.create(
                new Email(VALID_EMAIL),
                new PersonName("User"),
                new PersonName("Name"),
                new Password("Password@123"),
                UserProvider.LOCAL
        );
        user.markEmailAsVerified();
        return user;
    }

    private User buildUnverifiedUser() {
        return User.create(
                new Email(VALID_EMAIL),
                new PersonName("User"),
                new PersonName("Name"),
                new Password("Password@123"),
                UserProvider.LOCAL
        );
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve gerar token e publicar email quando usuário existe e email verificado")
        void execute_verifiedUser_generatesTokenAndQueuesEmail() {
            User user = buildVerifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(passwordResetTokenService.generatePasswordResetToken(user, DEVICE_INFO, IP_ADDRESS))
                    .willReturn("raw-reset-token");

            forgotPasswordUseCase.execute(validRequest(), DEVICE_INFO, IP_ADDRESS);

            ArgumentCaptor<EmailMessageRequest> captor =
                    ArgumentCaptor.forClass(EmailMessageRequest.class);
            then(producerService).should().producer(captor.capture());
            assertThat(captor.getValue().to()).isEqualTo(VALID_EMAIL);
        }

        @Test
        @DisplayName("deve revogar todos os tokens anteriores antes de gerar novo")
        void execute_verifiedUser_revokesExistingTokensBeforeGenerating() {
            User user = buildVerifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(passwordResetTokenService.generatePasswordResetToken(any(), any(), any()))
                    .willReturn("raw-reset-token");

            forgotPasswordUseCase.execute(validRequest(), DEVICE_INFO, IP_ADDRESS);

            org.mockito.InOrder order = org.mockito.Mockito.inOrder(
                    passwordResetTokenRepository, passwordResetTokenService);
            order.verify(passwordResetTokenRepository).revokeAllUserTokens(user);
            order.verify(passwordResetTokenService).generatePasswordResetToken(user, DEVICE_INFO, IP_ADDRESS);
        }

        @Test
        @DisplayName("deve incluir recoveryLink com o rawToken no email publicado")
        void execute_verifiedUser_includesRecoveryLinkInEmail() {
            User user = buildVerifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(passwordResetTokenService.generatePasswordResetToken(user, DEVICE_INFO, IP_ADDRESS))
                    .willReturn("raw-reset-token");

            forgotPasswordUseCase.execute(validRequest(), DEVICE_INFO, IP_ADDRESS);

            ArgumentCaptor<EmailMessageRequest> captor =
                    ArgumentCaptor.forClass(EmailMessageRequest.class);
            then(producerService).should().producer(captor.capture());
            assertThat(captor.getValue().variables())
                    .extractingByKey("recoveryLink")
                    .asString()
                    .contains("raw-reset-token");
        }

        @Test
        @DisplayName("deve lançar EmailNotVerifiedException quando email não verificado")
        void execute_emailNotVerified_throwsEmailNotVerifiedException() {
            User unverifiedUser = buildUnverifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(unverifiedUser));

            assertThatExceptionOfType(EmailNotVerifiedException.class)
                    .isThrownBy(() -> forgotPasswordUseCase.execute(validRequest(), DEVICE_INFO, IP_ADDRESS));

            then(passwordResetTokenService).should(never()).generatePasswordResetToken(any(), any(), any());
            then(producerService).should(never()).producer(any());
        }

        @Test
        @DisplayName("não deve lançar exceção quando usuário não encontrado (resposta silenciosa)")
        void execute_userNotFound_completesWithoutException() {
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.empty());

            assertThatNoException()
                    .isThrownBy(() -> forgotPasswordUseCase.execute(validRequest(), DEVICE_INFO, IP_ADDRESS));

            then(passwordResetTokenService).should(never()).generatePasswordResetToken(any(), any(), any());
            then(producerService).should(never()).producer(any());
        }

        @Test
        @DisplayName("não deve revogar tokens quando usuário não encontrado")
        void execute_userNotFound_doesNotRevokeTokens() {
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.empty());

            forgotPasswordUseCase.execute(validRequest(), DEVICE_INFO, IP_ADDRESS);

            then(passwordResetTokenRepository).should(never()).revokeAllUserTokens(any());
        }
    }
}