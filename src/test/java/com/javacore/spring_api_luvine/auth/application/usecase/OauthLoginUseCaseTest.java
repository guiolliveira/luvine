package com.javacore.spring_api_luvine.auth.application.usecase;

import com.javacore.spring_api_luvine.auth.application.dto.LoginResponse;
import com.javacore.spring_api_luvine.auth.application.service.TokenService;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("OauthLoginUseCase")
@ExtendWith(MockitoExtension.class)
class OauthLoginUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private TokenService tokenService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OauthLoginUseCase oauthLoginUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final String VALID_EMAIL    = "user@example.com";
    private static final String VALID_NAME     = "João Silva";
    private static final String DEVICE_INFO    = "Mozilla/5.0";
    private static final String IP_ADDRESS     = "192.168.0.1";
    private static final String ACCESS_TOKEN   = "header.payload.signature";
    private static final String REFRESH_TOKEN  = "raw-refresh-token-value";

    private User buildGoogleUser() {
        User user = User.create(
                new Email(VALID_EMAIL),
                new PersonName("João"),
                new PersonName("Silva"),
                new Password("encoded-password"),
                UserProvider.GOOGLE
        );
        user.markEmailAsVerified();
        return user;
    }

    private User buildLocalUser() {
        return User.create(
                new Email(VALID_EMAIL),
                new PersonName("João"),
                new PersonName("Silva"),
                new Password("encoded-password"),
                UserProvider.LOCAL
        );
    }

    private void setupTokenMocks(User user) {
        given(tokenService.generateRefreshToken(user, DEVICE_INFO, IP_ADDRESS)).willReturn(REFRESH_TOKEN);
        given(tokenService.generateAccessToken(user)).willReturn(ACCESS_TOKEN);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        // --- USUÁRIO EXISTENTE ------------------------------------------------------------------

        @Test
        @DisplayName("deve retornar tokens quando usuário Google já existe")
        void execute_existingGoogleUser_returnsTokenPair() {
            User googleUser = buildGoogleUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(googleUser));
            setupTokenMocks(googleUser);

            LoginResponse response = oauthLoginUseCase.execute(VALID_EMAIL, VALID_NAME, DEVICE_INFO, IP_ADDRESS);

            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("não deve criar novo usuário quando usuário Google já existe")
        void execute_existingGoogleUser_doesNotSaveNewUser() {
            User googleUser = buildGoogleUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(googleUser));
            setupTokenMocks(googleUser);

            oauthLoginUseCase.execute(VALID_EMAIL, VALID_NAME, DEVICE_INFO, IP_ADDRESS);

            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ProviderConflictException quando usuário existe mas é LOCAL")
        void execute_existingLocalUser_throwsProviderConflictException() {
            User localUser = buildLocalUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(localUser));

            assertThatExceptionOfType(ProviderConflictException.class)
                    .isThrownBy(() -> oauthLoginUseCase.execute(VALID_EMAIL, VALID_NAME, DEVICE_INFO, IP_ADDRESS));

            then(tokenService).should(never()).generateAccessToken(any());
            then(tokenService).should(never()).generateRefreshToken(any(), any(), any());
        }

        // --- NOVO USUÁRIO ------------------------------------------------------------------

        @Test
        @DisplayName("deve criar novo usuário Google quando email não existe")
        void execute_newUser_savesNewGoogleUser() {
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.empty());
            given(passwordEncoder.encode(anyString())).willReturn("encoded-random-password");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            given(userRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);

            oauthLoginUseCase.execute(VALID_EMAIL, VALID_NAME, DEVICE_INFO, IP_ADDRESS);

            User saved = captor.getValue();
            assertThat(saved.getUserProvider()).isEqualTo(UserProvider.GOOGLE);
            assertThat(saved.isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("deve retornar tokens após criar novo usuário Google")
        void execute_newUser_returnsTokenPair() {
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.empty());
            given(passwordEncoder.encode(anyString())).willReturn("encoded-random-password");
            given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);

            LoginResponse response = oauthLoginUseCase.execute(VALID_EMAIL, VALID_NAME, DEVICE_INFO, IP_ADDRESS);

            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("deve marcar email como verificado ao criar novo usuário Google")
        void execute_newUser_marksEmailAsVerified() {
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.empty());
            given(passwordEncoder.encode(anyString())).willReturn("encoded-random-password");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            given(userRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);

            oauthLoginUseCase.execute(VALID_EMAIL, VALID_NAME, DEVICE_INFO, IP_ADDRESS);

            assertThat(captor.getValue().isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("deve sanitizar nome removendo caracteres não alfabéticos ao criar usuário")
        void execute_newUser_sanitizesNameBeforePersisting() {
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.empty());
            given(passwordEncoder.encode(anyString())).willReturn("encoded-random-password");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            given(userRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);

            oauthLoginUseCase.execute(VALID_EMAIL, "João123 Silva!", DEVICE_INFO, IP_ADDRESS);

            User saved = captor.getValue();
            assertThat(saved.getFirstName().value()).doesNotContainAnyWhitespaces();
            assertThat(saved.getLastName().value()).doesNotContainAnyWhitespaces();
        }

        @Test
        @DisplayName("deve gerar password aleatório para novo usuário Google")
        void execute_newUser_encodesRandomPassword() {
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.empty());
            given(passwordEncoder.encode(anyString())).willReturn("encoded-random-password");
            given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);

            oauthLoginUseCase.execute(VALID_EMAIL, VALID_NAME, DEVICE_INFO, IP_ADDRESS);

            then(passwordEncoder).should(times(1)).encode(anyString());
        }

        // --- TOKENS ------------------------------------------------------------------

        @Test
        @DisplayName("deve aceitar deviceInfo e ipAddress nulos sem lançar exceção")
        void execute_nullDeviceAndIp_doesNotThrow() {
            User googleUser = buildGoogleUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(googleUser));
            given(tokenService.generateRefreshToken(googleUser, null, null)).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(googleUser)).willReturn(ACCESS_TOKEN);

            LoginResponse response = oauthLoginUseCase.execute(VALID_EMAIL, VALID_NAME, null, null);

            assertThat(response).isNotNull();
        }
    }
}