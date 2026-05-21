package com.javacore.spring_api_luvine.auth.application.usecase;

import com.javacore.spring_api_luvine.auth.application.dto.UpdatePasswordRequest;
import com.javacore.spring_api_luvine.auth.domain.entity.PasswordResetToken;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidTokenException;
import com.javacore.spring_api_luvine.auth.domain.exception.PasswordMisMatchException;
import com.javacore.spring_api_luvine.auth.infrastructure.repository.PasswordResetTokenRepository;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Password;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("ResetPasswordUseCase")
@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseTest {

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private ResetPasswordUseCase resetPasswordUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final String VALID_TOKEN    = "valid-raw-token";
    private static final String VALID_PASSWORD = "Password@123";

    private UpdatePasswordRequest validRequest() {
        return new UpdatePasswordRequest(VALID_TOKEN, VALID_PASSWORD, VALID_PASSWORD);
    }

    private User buildVerifiedUser() {
        User user = User.create(
                new Email("user@example.com"),
                new PersonName("User"),
                new PersonName("Name"),
                new Password(VALID_PASSWORD),
                UserProvider.LOCAL
        );
        user.markEmailAsVerified();
        return user;
    }

    private PasswordResetToken buildActiveToken(User user) {
        PasswordResetToken token = mock(PasswordResetToken.class);
        given(token.getExpiresAt()).willReturn(Instant.now().plus(1, ChronoUnit.HOURS));
        given(token.getUser()).willReturn(user);
        return token;
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve alterar senha com sucesso quando token válido e senhas coincidem")
        void execute_validRequest_completesWithoutException() {
            User user = buildVerifiedUser();
            PasswordResetToken token = buildActiveToken(user);

            given(passwordResetTokenRepository.findByTokenAndUsedFalseAndRevokedFalse(anyString()))
                    .willReturn(Optional.of(token));
            given(passwordEncoder.encode(VALID_PASSWORD)).willReturn("NewEncoded@456");

            assertThatNoException().isThrownBy(() -> resetPasswordUseCase.execute(validRequest()));
        }

        @Test
        @DisplayName("deve marcar token como usado após alterar senha")
        void execute_validRequest_marksTokenAsUsed() {
            User user = buildVerifiedUser();
            PasswordResetToken token = buildActiveToken(user);

            given(passwordResetTokenRepository.findByTokenAndUsedFalseAndRevokedFalse(anyString()))
                    .willReturn(Optional.of(token));
            given(passwordEncoder.encode(VALID_PASSWORD)).willReturn("NewEncoded@456");

            resetPasswordUseCase.execute(validRequest());

            then(token).should().markAsUsed();
            then(passwordResetTokenRepository).should().save(token);
        }

        @Test
        @DisplayName("deve lançar PasswordMisMatchException quando senhas não coincidem")
        void execute_passwordMismatch_throwsPasswordMisMatchException() {
            UpdatePasswordRequest request = new UpdatePasswordRequest(
                    VALID_TOKEN, "Password@123", "Different@456"
            );

            assertThatExceptionOfType(PasswordMisMatchException.class)
                    .isThrownBy(() -> resetPasswordUseCase.execute(request));

            then(passwordResetTokenRepository).should(never()).findByTokenAndUsedFalseAndRevokedFalse(any());
        }

        @Test
        @DisplayName("deve lançar InvalidTokenException quando token não encontrado ou já usado")
        void execute_tokenNotFound_throwsInvalidTokenException() {
            given(passwordResetTokenRepository.findByTokenAndUsedFalseAndRevokedFalse(anyString()))
                    .willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidTokenException.class)
                    .isThrownBy(() -> resetPasswordUseCase.execute(validRequest()));

            then(passwordEncoder).should(never()).encode(anyString());
        }

        @Test
        @DisplayName("deve lançar InvalidTokenException quando token expirou")
        void execute_expiredToken_throwsInvalidTokenException() {
            PasswordResetToken expiredToken = mock(PasswordResetToken.class);
            given(expiredToken.getExpiresAt()).willReturn(Instant.now().minus(1, ChronoUnit.HOURS));

            given(passwordResetTokenRepository.findByTokenAndUsedFalseAndRevokedFalse(anyString()))
                    .willReturn(Optional.of(expiredToken));

            assertThatExceptionOfType(InvalidTokenException.class)
                    .isThrownBy(() -> resetPasswordUseCase.execute(validRequest()));

            then(expiredToken).should(never()).markAsUsed();
            then(passwordEncoder).should(never()).encode(anyString());
        }

        @Test
        @DisplayName("deve lançar InvalidPasswordException quando nova senha não atende os requisitos")
        void execute_invalidPassword_throwsInvalidPasswordException() {
            UpdatePasswordRequest request = new UpdatePasswordRequest(
                    VALID_TOKEN, "fraca", "fraca"
            );

            assertThatExceptionOfType(Exception.class)
                    .isThrownBy(() -> resetPasswordUseCase.execute(request));

            then(passwordResetTokenRepository).should(never()).findByTokenAndUsedFalseAndRevokedFalse(any());
        }

        @Test
        @DisplayName("deve encodar a nova senha antes de persistir")
        void execute_validRequest_encodesPasswordBeforePersisting() {
            User user = buildVerifiedUser();
            PasswordResetToken token = buildActiveToken(user);

            given(passwordResetTokenRepository.findByTokenAndUsedFalseAndRevokedFalse(anyString()))
                    .willReturn(Optional.of(token));
            given(passwordEncoder.encode(VALID_PASSWORD)).willReturn("NewEncoded@456");

            resetPasswordUseCase.execute(validRequest());

            then(passwordEncoder).should(times(1)).encode(VALID_PASSWORD);
        }
    }
}