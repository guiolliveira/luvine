package com.javacore.spring_api_luvine.auth.application.usecase;

import com.javacore.spring_api_luvine.auth.application.dto.VerifyEmailRequest;
import com.javacore.spring_api_luvine.auth.application.service.EmailVerificationService;
import com.javacore.spring_api_luvine.auth.domain.exception.EmailAlreadyVerifiedException;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidCodeException;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidCredentialsException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("VerifyEmailUseCase")
@ExtendWith(MockitoExtension.class)
class VerifyEmailUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailVerificationService verificationService;

    @InjectMocks
    private VerifyEmailUseCase verifyEmailUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_CODE = "123456";

    private VerifyEmailRequest validRequest() {
        return new VerifyEmailRequest(VALID_EMAIL, VALID_CODE);
    }

    private User buildUnverifiedUser() {
        User user = User.create(
                new Email(VALID_EMAIL),
                new PersonName("User"),
                new PersonName("Name"),
                new Password("Password@123"),
                UserProvider.LOCAL
        );
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private User buildVerifiedUser() {
        User user = buildUnverifiedUser();
        user.markEmailAsVerified();
        return user;
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve verificar email com sucesso sem lançar exceção")
        void execute_validCode_completesWithoutException() {
            User user = buildUnverifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            willDoNothing().given(verificationService).validateCode(anyLong(), anyString());

            assertThatNoException().isThrownBy(() -> verifyEmailUseCase.execute(validRequest()));
            then(verificationService).should().validateCode(user.getId(), VALID_CODE);
        }

        @Test
        @DisplayName("deve lançar InvalidCredentialsException quando usuário não encontrado")
        void execute_userNotFound_throwsInvalidCredentialsException() {
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> verifyEmailUseCase.execute(validRequest()));

            then(verificationService).should(never()).validateCode(anyLong(), anyString());
        }

        @Test
        @DisplayName("deve lançar EmailAlreadyVerifiedException quando email já verificado")
        void execute_alreadyVerified_throwsEmailAlreadyVerifiedException() {
            User verifiedUser = buildVerifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(verifiedUser));

            assertThatExceptionOfType(EmailAlreadyVerifiedException.class)
                    .isThrownBy(() -> verifyEmailUseCase.execute(validRequest()));

            then(verificationService).should(never()).validateCode(anyLong(), anyString());
        }

        @Test
        @DisplayName("deve propagar InvalidCodeException quando código inválido")
        void execute_invalidCode_propagatesInvalidCodeException() {
            User user = buildUnverifiedUser();
            VerifyEmailRequest request = new VerifyEmailRequest(VALID_EMAIL, "999999");

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            willThrow(new InvalidCodeException())
                    .given(verificationService).validateCode(anyLong(), eq("999999"));

            assertThatExceptionOfType(InvalidCodeException.class)
                    .isThrownBy(() -> verifyEmailUseCase.execute(request));
        }

        @Test
        @DisplayName("deve passar o id do usuário correto para o verificationService")
        void execute_validRequest_passesCorrectUserIdToVerificationService() {
            User user = buildUnverifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            willDoNothing().given(verificationService).validateCode(anyLong(), anyString());

            verifyEmailUseCase.execute(validRequest());

            then(verificationService).should().validateCode(1L, VALID_CODE);
        }
    }
}