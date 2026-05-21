package com.javacore.spring_api_luvine.auth.application.usecase;

import com.javacore.spring_api_luvine.auth.application.dto.EmailVerificationCreationResult;
import com.javacore.spring_api_luvine.auth.application.dto.ResendEmailRequest;
import com.javacore.spring_api_luvine.auth.application.service.EmailVerificationService;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidCredentialsException;
import com.javacore.spring_api_luvine.auth.domain.exception.RateLimitExceededException;
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

@DisplayName("ResendEmailUseCase")
@ExtendWith(MockitoExtension.class)
class ResendEmailUseCaseTest {

    @Mock private EmailVerificationService verificationService;
    @Mock private ProducerService producerService;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ResendEmailUseCase resendEmailUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_CODE = "654321";

    private ResendEmailRequest validRequest() {
        return new ResendEmailRequest(VALID_EMAIL);
    }

    private User buildUser() {
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
        @DisplayName("deve reenviar email de verificação com sucesso sem lançar exceção")
        void execute_existingUser_completesWithoutException() {
            User user = buildUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(verificationService.createCode(user))
                    .willReturn(new EmailVerificationCreationResult(null, VALID_CODE));

            assertThatNoException().isThrownBy(() -> resendEmailUseCase.execute(validRequest()));
        }

        @Test
        @DisplayName("deve publicar email com o destinatário correto")
        void execute_existingUser_publishesEmailToCorrectRecipient() {
            User user = buildUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(verificationService.createCode(user))
                    .willReturn(new EmailVerificationCreationResult(null, VALID_CODE));

            resendEmailUseCase.execute(validRequest());

            ArgumentCaptor<EmailMessageRequest> captor =
                    ArgumentCaptor.forClass(EmailMessageRequest.class);
            then(producerService).should().producer(captor.capture());
            assertThat(captor.getValue().to()).isEqualTo(VALID_EMAIL);
        }

        @Test
        @DisplayName("deve lançar InvalidCredentialsException quando usuário não encontrado")
        void execute_userNotFound_throwsInvalidCredentialsException() {
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> resendEmailUseCase.execute(validRequest()));

            then(verificationService).should(never()).createCode(any());
            then(producerService).should(never()).producer(any());
        }

        @Test
        @DisplayName("deve propagar RateLimitExceededException quando rate limit atingido")
        void execute_rateLimitExceeded_propagatesRateLimitExceededException() {
            User user = buildUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(verificationService.createCode(user)).willThrow(new RateLimitExceededException());

            assertThatExceptionOfType(RateLimitExceededException.class)
                    .isThrownBy(() -> resendEmailUseCase.execute(validRequest()));

            then(producerService).should(never()).producer(any());
        }

        @Test
        @DisplayName("deve reenviar para usuário já verificado sem verificar o status")
        void execute_verifiedUser_stillSendsEmail() {
            User verifiedUser = buildUser();
            verifiedUser.markEmailAsVerified();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(verifiedUser));
            given(verificationService.createCode(verifiedUser))
                    .willReturn(new EmailVerificationCreationResult(null, VALID_CODE));

            assertThatNoException().isThrownBy(() -> resendEmailUseCase.execute(validRequest()));
            then(producerService).should().producer(any());
        }

        @Test
        @DisplayName("deve chamar producerService exatamente uma vez por reenvio")
        void execute_existingUser_callsProducerExactlyOnce() {
            User user = buildUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(verificationService.createCode(user))
                    .willReturn(new EmailVerificationCreationResult(null, VALID_CODE));

            resendEmailUseCase.execute(validRequest());

            then(producerService).should(times(1)).producer(any());
        }
    }
}