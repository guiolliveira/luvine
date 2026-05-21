package com.javacore.spring_api_luvine.auth.application.usecase;

import com.javacore.spring_api_luvine.auth.application.dto.EmailVerificationCreationResult;
import com.javacore.spring_api_luvine.auth.application.dto.RegisterRequest;
import com.javacore.spring_api_luvine.auth.application.dto.RegisterResponse;
import com.javacore.spring_api_luvine.auth.application.mapper.AuthMapper;
import com.javacore.spring_api_luvine.auth.application.service.EmailVerificationService;
import com.javacore.spring_api_luvine.auth.domain.exception.EmailAlreadyExistsException;
import com.javacore.spring_api_luvine.auth.domain.exception.PasswordMisMatchException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("RegisterUserUseCase")
@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthMapper authMapper;
    @Mock private EmailVerificationService verificationService;
    @Mock private ProducerService producerService;

    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final String VALID_EMAIL      = "user@example.com";
    private static final String VALID_FIRST_NAME = "User";
    private static final String VALID_LAST_NAME  = "Name";
    private static final String VALID_PASSWORD   = "Password@123";

    private User buildPersistedUnverifiedUser() {
        User user = User.create(
                new Email(VALID_EMAIL),
                new PersonName(VALID_FIRST_NAME),
                new PersonName(VALID_LAST_NAME),
                new Password("encoded-password"),
                UserProvider.LOCAL
        );
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private RegisterRequest validRequest() {
        return new RegisterRequest(VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_PASSWORD, VALID_PASSWORD);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve registrar usuário com sucesso e publicar email de verificação")
        void execute_validRequest_returnsRegisterResponseAndPublishesEmail() {
            User savedUser = buildPersistedUnverifiedUser();

            given(userRepository.existsByEmail(new Email(VALID_EMAIL))).willReturn(false);
            given(passwordEncoder.encode(VALID_PASSWORD)).willReturn("encoded-password");
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(verificationService.createCode(any(User.class)))
                    .willReturn(new EmailVerificationCreationResult(null, "123456"));

            RegisterResponse expectedResponse = new RegisterResponse(
                    UUID.randomUUID(), VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME,
                    Instant.now(), true, UserProvider.LOCAL
            );
            given(authMapper.toRegisterResponse(any(User.class))).willReturn(expectedResponse);

            RegisterResponse response = registerUserUseCase.execute(validRequest());

            assertThat(response).isNotNull().isEqualTo(expectedResponse);
            then(userRepository).should().save(any(User.class));

            ArgumentCaptor<EmailMessageRequest> emailCaptor =
                    ArgumentCaptor.forClass(EmailMessageRequest.class);
            then(producerService).should().producer(emailCaptor.capture());
            assertThat(emailCaptor.getValue().to()).isEqualTo(VALID_EMAIL);
        }

        @Test
        @DisplayName("deve lançar EmailAlreadyExistsException quando email já cadastrado")
        void execute_emailAlreadyExists_throwsEmailAlreadyExistsException() {
            given(userRepository.existsByEmail(new Email(VALID_EMAIL))).willReturn(true);

            assertThatExceptionOfType(EmailAlreadyExistsException.class)
                    .isThrownBy(() -> registerUserUseCase.execute(validRequest()));

            then(userRepository).should(never()).save(any());
            then(producerService).should(never()).producer(any());
        }

        @Test
        @DisplayName("deve lançar PasswordMisMatchException quando senhas não coincidem")
        void execute_passwordMismatch_throwsPasswordMisMatchException() {
            RegisterRequest request = new RegisterRequest(
                    VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME, "Password@123", "Password@456"
            );
            given(userRepository.existsByEmail(new Email(VALID_EMAIL))).willReturn(false);

            assertThatExceptionOfType(PasswordMisMatchException.class)
                    .isThrownBy(() -> registerUserUseCase.execute(request));

            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando email é inválido")
        void execute_invalidEmail_throwsException() {
            RegisterRequest request = new RegisterRequest(
                    "not-an-email", VALID_FIRST_NAME, VALID_LAST_NAME, VALID_PASSWORD, VALID_PASSWORD
            );

            assertThatException().isThrownBy(() -> registerUserUseCase.execute(request));
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("não deve publicar email quando verificationService lança exceção")
        void execute_verificationServiceThrows_doesNotPublishEmail() {
            given(userRepository.existsByEmail(new Email(VALID_EMAIL))).willReturn(false);
            given(passwordEncoder.encode(any())).willReturn("encoded");
            given(verificationService.createCode(any())).willThrow(new RateLimitExceededException());

            assertThatExceptionOfType(RateLimitExceededException.class)
                    .isThrownBy(() -> registerUserUseCase.execute(validRequest()));

            then(producerService).should(never()).producer(any());
        }
    }
}