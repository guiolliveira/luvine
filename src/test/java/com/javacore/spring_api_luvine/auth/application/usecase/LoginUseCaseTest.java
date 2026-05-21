package com.javacore.spring_api_luvine.auth.application.usecase;

import com.javacore.spring_api_luvine.auth.application.dto.LoginRequest;
import com.javacore.spring_api_luvine.auth.application.dto.LoginResponse;
import com.javacore.spring_api_luvine.auth.application.service.TokenService;
import com.javacore.spring_api_luvine.auth.domain.exception.EmailNotVerifiedException;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidCredentialsException;
import com.javacore.spring_api_luvine.auth.domain.exception.ProviderConflictException;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("LoginUseCase")
@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock private TokenService tokenService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private LoginUseCase loginUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_PASSWORD = "Password@123";
    private static final String DEVICE_INFO = "Mozilla/5.0";
    private static final String IP_ADDRESS = "192.168.0.1";
    private static final String ACCESS_TOKEN = "header.payload.signature";
    private static final String REFRESH_TOKEN = "raw-refresh-token-value";

    private LoginRequest validRequest() {
        return new LoginRequest(VALID_EMAIL, VALID_PASSWORD);
    }

    private User buildVerifiedLocalUser() {
        User user = User.create(
                new Email(VALID_EMAIL),
                new PersonName("User"),
                new PersonName("Name"),
                new Password(VALID_PASSWORD),
                UserProvider.LOCAL
        );
        user.markEmailAsVerified();
        return user;
    }

    private User buildUnverifiedLocalUser() {
        return User.create(
                new Email(VALID_EMAIL),
                new PersonName("User"),
                new PersonName("Name"),
                new Password(VALID_PASSWORD),
                UserProvider.LOCAL
        );
    }

    private User buildVerifiedGoogleUser() {
        User user = User.create(
                new Email(VALID_EMAIL),
                new PersonName("User"),
                new PersonName("Name"),
                new Password(VALID_PASSWORD),
                UserProvider.GOOGLE
        );
        user.markEmailAsVerified();
        return user;
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve retornar tokens quando credenciais são válidas e email verificado")
        void execute_validCredentials_returnsTokenPair() {
            User user = buildVerifiedLocalUser();

            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(tokenService.generateRefreshToken(user, DEVICE_INFO, IP_ADDRESS)).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(user)).willReturn(ACCESS_TOKEN);

            LoginResponse response = loginUseCase.execute(validRequest(), DEVICE_INFO, IP_ADDRESS);

            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("deve lançar InvalidCredentialsException quando autenticação falha")
        void execute_badCredentials_throwsInvalidCredentialsException() {
            given(authenticationManager.authenticate(any()))
                    .willThrow(new BadCredentialsException("bad credentials"));

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> loginUseCase.execute(validRequest(), DEVICE_INFO, IP_ADDRESS));

            then(tokenService).should(never()).generateAccessToken(any());
            then(tokenService).should(never()).generateRefreshToken(any(), any(), any());
        }

        @Test
        @DisplayName("deve lançar InvalidCredentialsException quando usuário não encontrado")
        void execute_userNotFound_throwsInvalidCredentialsException() {
            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> loginUseCase.execute(validRequest(), DEVICE_INFO, IP_ADDRESS));

            then(tokenService).should(never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("deve lançar ProviderConflictException quando usuário é OAuth (não LOCAL)")
        void execute_oauthUser_throwsProviderConflictException() {
            User googleUser = buildVerifiedGoogleUser();

            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(googleUser));

            assertThatExceptionOfType(ProviderConflictException.class)
                    .isThrownBy(() -> loginUseCase.execute(validRequest(), DEVICE_INFO, IP_ADDRESS));

            then(tokenService).should(never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("deve lançar EmailNotVerifiedException quando email não verificado")
        void execute_emailNotVerified_throwsEmailNotVerifiedException() {
            User unverifiedUser = buildUnverifiedLocalUser();

            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(unverifiedUser));

            assertThatExceptionOfType(EmailNotVerifiedException.class)
                    .isThrownBy(() -> loginUseCase.execute(validRequest(), DEVICE_INFO, IP_ADDRESS));

            then(tokenService).should(never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("deve passar o authenticationToken correto para o AuthenticationManager")
        void execute_validRequest_authenticatesWithCorrectToken() {
            User user = buildVerifiedLocalUser();

            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);

            loginUseCase.execute(validRequest(), DEVICE_INFO, IP_ADDRESS);

            ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            then(authenticationManager).should().authenticate(captor.capture());

            assertThat(captor.getValue().getPrincipal()).isEqualTo(VALID_EMAIL);
            assertThat(captor.getValue().getCredentials()).isEqualTo(VALID_PASSWORD);
        }

        @Test
        @DisplayName("deve aceitar deviceInfo e ipAddress nulos sem lançar exceção")
        void execute_nullDeviceAndIp_doesNotThrow() {
            User user = buildVerifiedLocalUser();

            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(tokenService.generateRefreshToken(user, null, null)).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(user)).willReturn(ACCESS_TOKEN);

            assertThatNoException()
                    .isThrownBy(() -> loginUseCase.execute(validRequest(), null, null));
        }
    }
}