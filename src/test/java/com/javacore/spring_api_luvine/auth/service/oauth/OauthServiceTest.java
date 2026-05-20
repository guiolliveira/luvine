package com.javacore.spring_api_luvine.auth.service.oauth;

import com.javacore.spring_api_luvine.auth.application.service.oauth.OauthService;
import com.javacore.spring_api_luvine.auth.domain.exception.ProviderConflictException;
import com.javacore.spring_api_luvine.auth.application.dto.LoginResponse;
import com.javacore.spring_api_luvine.auth.application.service.TokenService;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OauthService")
class OauthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private TokenService tokenService;

    @InjectMocks
    private OauthService oauthService;

    private static final String EMAIL = "user@example.com";
    private static final String NAME = "user name";
    private static final String DEVICE_INFO = "Mozilla/5.0";
    private static final String IP_ADDRESS = "192.168.0.1";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    private User mockGoogleUser() {
        User user = mock(User.class);
        given(user.getPublicId()).willReturn(UUID.randomUUID());
        given(user.getUserProvider()).willReturn(UserProvider.GOOGLE);
        return user;
    }

    // --- LOGIN WITH GOOGLE ---------------------------------------------------------

    @Nested
    @DisplayName("loginWithGoogle()")
    class LoginWithGoogle {

        @Test
        @DisplayName("deve retornar tokens quando usuário Google já existe")
        void loginWithGoogle_existingGoogleUser_returnsTokenPair() {
            User existingUser = mockGoogleUser();

            given(userRepository.findByEmail(new Email(EMAIL))).willReturn(Optional.of(existingUser));
            given(tokenService.generateRefreshToken(existingUser, DEVICE_INFO, IP_ADDRESS))
                    .willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(existingUser)).willReturn(ACCESS_TOKEN);

            LoginResponse response = oauthService.loginWithGoogle(EMAIL, NAME, DEVICE_INFO, IP_ADDRESS);

            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("deve criar novo usuário quando email não existe e retornar tokens")
        void loginWithGoogle_newUser_persistsAndReturnsTokenPair() {
            given(userRepository.findByEmail(new Email(EMAIL))).willReturn(Optional.empty());
            given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));
            given(tokenService.generateRefreshToken(any(), anyString(), anyString()))
                    .willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);

            LoginResponse response = oauthService.loginWithGoogle(EMAIL, NAME, DEVICE_INFO, IP_ADDRESS);

            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
            then(userRepository).should().save(any(User.class));
        }

        @Test
        @DisplayName("deve criar novo usuário com email verificado")
        void loginWithGoogle_newUser_savesUserWithEmailVerified() {
            given(userRepository.findByEmail(new Email(EMAIL))).willReturn(Optional.empty());
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            given(userRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);

            oauthService.loginWithGoogle(EMAIL, NAME, DEVICE_INFO, IP_ADDRESS);

            assertThat(captor.getValue().isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("deve criar novo usuário com provider GOOGLE")
        void loginWithGoogle_newUser_savesUserWithGoogleProvider() {
            given(userRepository.findByEmail(new Email(EMAIL))).willReturn(Optional.empty());
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            given(userRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);

            oauthService.loginWithGoogle(EMAIL, NAME, DEVICE_INFO, IP_ADDRESS);

            assertThat(captor.getValue().getUserProvider()).isEqualTo(UserProvider.GOOGLE);
        }

        @Test
        @DisplayName("deve extrair firstName e lastName corretamente do nome completo")
        void loginWithGoogle_newUser_splitsNameCorrectly() {
            given(userRepository.findByEmail(new Email(EMAIL))).willReturn(Optional.empty());
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            given(userRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);

            oauthService.loginWithGoogle(EMAIL, "user name", DEVICE_INFO, IP_ADDRESS);

            User saved = captor.getValue();
            assertThat(saved.getFirstName().value()).isEqualTo("User");
            assertThat(saved.getLastName().value()).isEqualTo("Name");
        }

        @Test
        @DisplayName("deve usar firstName e lastName vazio quando nome tem apenas uma palavra")
        void loginWithGoogle_newUser_singleWordName_setsEmptyLastName() {
            given(userRepository.findByEmail(new Email(EMAIL))).willReturn(Optional.empty());
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            given(userRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);

            oauthService.loginWithGoogle(EMAIL, "user", DEVICE_INFO, IP_ADDRESS);

            User saved = captor.getValue();
            assertThat(saved.getFirstName().value()).isEqualTo("User");
            assertThat(saved.getLastName().value()).isBlank();
        }

        @Test
        @DisplayName("deve lançar ProviderConflictException quando usuário existe com provider diferente de GOOGLE")
        void loginWithGoogle_existingLocalUser_throwsProviderConflictException() {
            User localUser = mock(User.class);
            given(localUser.getPublicId()).willReturn(UUID.randomUUID());
            given(localUser.getUserProvider()).willReturn(UserProvider.LOCAL);

            given(userRepository.findByEmail(new Email(EMAIL))).willReturn(Optional.of(localUser));

            assertThatExceptionOfType(ProviderConflictException.class)
                    .isThrownBy(() -> oauthService.loginWithGoogle(EMAIL, NAME, DEVICE_INFO, IP_ADDRESS));

            then(tokenService).should(never()).generateAccessToken(any());
            then(tokenService).should(never()).generateRefreshToken(any(), any(), any());
        }

        @Test
        @DisplayName("não deve persistir usuário quando provider conflict é detectado")
        void loginWithGoogle_existingLocalUser_doesNotSaveUser() {
            User localUser = mock(User.class);
            given(localUser.getPublicId()).willReturn(UUID.randomUUID());
            given(localUser.getUserProvider()).willReturn(UserProvider.LOCAL);

            given(userRepository.findByEmail(new Email(EMAIL))).willReturn(Optional.of(localUser));

            assertThatExceptionOfType(ProviderConflictException.class)
                    .isThrownBy(() -> oauthService.loginWithGoogle(EMAIL, NAME, DEVICE_INFO, IP_ADDRESS));

            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve normalizar email para lowercase antes de buscar no repositório")
        void loginWithGoogle_upperCaseEmail_normalizesBeforeQuery() {
            User existingUser = mockGoogleUser();

            given(userRepository.findByEmail(new Email("USER@EXAMPLE.COM"))).willReturn(Optional.of(existingUser));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);

            oauthService.loginWithGoogle("USER@EXAMPLE.COM", NAME, DEVICE_INFO, IP_ADDRESS);

            then(userRepository).should().findByEmail(new Email("USER@EXAMPLE.COM"));
        }

        @Test
        @DisplayName("deve aceitar deviceInfo e ipAddress nulos sem lançar exceção")
        void loginWithGoogle_nullDeviceAndIp_doesNotThrow() {
            User existingUser = mockGoogleUser();

            given(userRepository.findByEmail(new Email(EMAIL))).willReturn(Optional.of(existingUser));
            given(tokenService.generateRefreshToken(existingUser, null, null)).willReturn(REFRESH_TOKEN);
            given(tokenService.generateAccessToken(existingUser)).willReturn(ACCESS_TOKEN);

            assertThatNoException()
                    .isThrownBy(() -> oauthService.loginWithGoogle(EMAIL, NAME, null, null));
        }
    }
}