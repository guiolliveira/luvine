package com.javacore.spring_api_luvine.auth.service;

import com.javacore.spring_api_luvine.auth.domain.entity.RefreshToken;
import com.javacore.spring_api_luvine.auth.domain.exception.*;
import com.javacore.spring_api_luvine.auth.dto.*;
import com.javacore.spring_api_luvine.auth.mapper.AuthMapper;
import com.javacore.spring_api_luvine.auth.repository.PasswordResetTokenRepository;
import com.javacore.spring_api_luvine.auth.repository.RefreshTokenRepository;
import com.javacore.spring_api_luvine.shared.messaging.dto.EmailMessageRequest;
import com.javacore.spring_api_luvine.shared.messaging.service.producer.ProducerService;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Name;
import com.javacore.spring_api_luvine.user.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;


@DisplayName("AuthService")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthMapper authMapper;
    @Mock private TokenService tokenService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private EmailVerificationService verificationService;
    @Mock private ProducerService producerService;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordResetTokenService passwordResetTokenService;

    @InjectMocks
    private AuthService authService;

    // --- HELPERS ------------------------------------------------------------------

    private static final String VALID_EMAIL        = "user@example.com";
    private static final String VALID_FIRST_NAME   = "User";
    private static final String VALID_LAST_NAME    = "Name";
    private static final String VALID_PASSWORD     = "password@123";
    private static final String DEVICE_INFO        = "Mozilla/5.0";
    private static final String IP_ADDRESS         = "192.168.0.1";
    private static final String ACCESS_TOKEN       = "header.payload.signature";
    private static final String REFRESH_TOKEN_RAW  = "raw-refresh-token-value";

    private Email email() { return new Email(VALID_EMAIL); }
    private Name firstName() { return new Name(VALID_FIRST_NAME); }
    private Name lastName()  { return new Name(VALID_LAST_NAME); }

    private User buildVerifiedUser() {
        User user = User.create(email(), firstName(), lastName(), "encoded-password", UserProvider.LOCAL);
        user.markEmailAsVerified();
        return user;
    }

    private User buildPersistedUnverifiedUser() {
        User user = User.create(email(), firstName(), lastName(), "encoded-password", UserProvider.LOCAL);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private RegisterRequest validRegisterRequest() {
        return new RegisterRequest(VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_PASSWORD, VALID_PASSWORD);
    }

    private LoginRequest validLoginRequest() {
        return new LoginRequest(VALID_EMAIL, VALID_PASSWORD);
    }

    // --- REGISTER ------------------------------------------------------------------

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("deve registrar usuário com sucesso e publicar email de verificação")
        void register_validRequest_returnsRegisterResponseAndPublishesEmail() {
            RegisterRequest request = validRegisterRequest();
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

            RegisterResponse response = authService.register(request);

            assertThat(response).isNotNull().isEqualTo(expectedResponse);

            then(userRepository).should().save(any(User.class));

            ArgumentCaptor<EmailMessageRequest> emailCaptor =
                    ArgumentCaptor.forClass(EmailMessageRequest.class);
            then(producerService).should().producer(emailCaptor.capture());

            EmailMessageRequest publishedEmail = emailCaptor.getValue();
            assertThat(publishedEmail.to()).isEqualTo(VALID_EMAIL);
        }

        @Test
        @DisplayName("deve lançar EmailAlreadyExistsException quando email já cadastrado")
        void register_emailAlreadyExists_throwsEmailAlreadyExistsException() {
            given(userRepository.existsByEmail(new Email(VALID_EMAIL))).willReturn(true);

            assertThatExceptionOfType(EmailAlreadyExistsException.class)
                    .isThrownBy(() -> authService.register(validRegisterRequest()));

            then(userRepository).should(never()).save(any());
            then(producerService).should(never()).producer(any());
        }

        @Test
        @DisplayName("deve lançar PasswordMisMatchException quando senhas não coincidem")
        void register_passwordMismatch_throwsPasswordMisMatchException() {
            RegisterRequest request = new RegisterRequest(
                    VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME, "password@123", "password@456"
            );
            given(userRepository.existsByEmail(new Email(VALID_EMAIL))).willReturn(false);

            assertThatExceptionOfType(PasswordMisMatchException.class)
                    .isThrownBy(() -> authService.register(request));

            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando email é inválido")
        void register_invalidEmail_throwsException() {
            RegisterRequest request = new RegisterRequest(
                    "not-an-email", VALID_FIRST_NAME, VALID_LAST_NAME, VALID_PASSWORD, VALID_PASSWORD
            );

            assertThatException().isThrownBy(() -> authService.register(request));
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("não deve publicar email quando verificationService lança exceção")
        void register_verificationServiceThrows_doesNotPublishEmail() {
            given(userRepository.existsByEmail(new Email(VALID_EMAIL))).willReturn(false);
            given(passwordEncoder.encode(any())).willReturn("encoded");
            given(verificationService.createCode(any())).willThrow(new RateLimitExceededException());

            assertThatExceptionOfType(RateLimitExceededException.class)
                    .isThrownBy(() -> authService.register(validRegisterRequest()));

            then(producerService).should(never()).producer(any());
        }
    }

    // --- LOGIN ------------------------------------------------------------------

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("deve retornar tokens quando credenciais são válidas e email verificado")
        void login_validCredentials_returnsTokenPair() {
            User user = buildVerifiedUser();
            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(tokenService.generateRefreshToken(user, DEVICE_INFO, IP_ADDRESS)).willReturn(REFRESH_TOKEN_RAW);
            given(tokenService.generateAccessToken(user)).willReturn(ACCESS_TOKEN);

            LoginResponse response = authService.login(validLoginRequest(), DEVICE_INFO, IP_ADDRESS);

            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN_RAW);
        }

        @Test
        @DisplayName("deve lançar InvalidCredentialsException quando autenticação falha")
        void login_badCredentials_throwsInvalidCredentialsException() {
            given(authenticationManager.authenticate(any()))
                    .willThrow(new BadCredentialsException("bad credentials"));

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> authService.login(validLoginRequest(), DEVICE_INFO, IP_ADDRESS));

            then(tokenService).should(never()).generateAccessToken(any());
            then(tokenService).should(never()).generateRefreshToken(any(), any(), any());
        }

        @Test
        @DisplayName("deve lançar EmailNotVerifiedException quando email não verificado")
        void login_emailNotVerified_throwsEmailNotVerifiedException() {
            User unverifiedUser = buildPersistedUnverifiedUser();
            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(unverifiedUser));

            assertThatExceptionOfType(EmailNotVerifiedException.class)
                    .isThrownBy(() -> authService.login(validLoginRequest(), DEVICE_INFO, IP_ADDRESS));

            then(tokenService).should(never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("deve lançar ProviderConflictException quando usuário é OAuth (não LOCAL)")
        void login_oauthUser_throwsProviderConflictException() {
            User oauthUser = User.create(email(), firstName(), lastName(), "", UserProvider.GOOGLE);
            oauthUser.markEmailAsVerified();

            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(oauthUser));

            assertThatExceptionOfType(ProviderConflictException.class)
                    .isThrownBy(() -> authService.login(validLoginRequest(), DEVICE_INFO, IP_ADDRESS));
        }

        @Test
        @DisplayName("deve lançar InvalidCredentialsException quando usuário não encontrado")
        void login_userNotFound_throwsInvalidCredentialsException() {
            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> authService.login(validLoginRequest(), DEVICE_INFO, IP_ADDRESS));
        }

        @Test
        @DisplayName("deve passar o authenticationToken correto para o AuthenticationManager")
        void login_validRequest_authenticatesWithCorrectToken() {
            User user = buildVerifiedUser();
            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN_RAW);
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);

            authService.login(validLoginRequest(), DEVICE_INFO, IP_ADDRESS);

            ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            then(authenticationManager).should().authenticate(authCaptor.capture());

            assertThat(authCaptor.getValue().getPrincipal()).isEqualTo(VALID_EMAIL);
            assertThat(authCaptor.getValue().getCredentials()).isEqualTo(VALID_PASSWORD);
        }

        @Test
        @DisplayName("deve aceitar deviceInfo e ipAddress nulos sem lançar exceção")
        void login_nullDeviceAndIp_doesNotThrow() {
            User user = buildVerifiedUser();
            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(tokenService.generateRefreshToken(user, null, null)).willReturn(REFRESH_TOKEN_RAW);
            given(tokenService.generateAccessToken(user)).willReturn(ACCESS_TOKEN);

            assertThatNoException()
                    .isThrownBy(() -> authService.login(validLoginRequest(), null, null));
        }
    }

    // --- REFRESH ------------------------------------------------------------------

    @Nested
    @DisplayName("refresh()")
    class Refresh {

        private RefreshToken buildActiveToken(User user) {
            return RefreshToken.create(user, "hashed-token", DEVICE_INFO, IP_ADDRESS);
        }

        @Test
        @DisplayName("deve retornar novos tokens quando refresh token é válido")
        void refresh_validToken_returnsNewTokenPair() {
            User user = buildVerifiedUser();
            RefreshToken activeToken = buildActiveToken(user);

            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.of(activeToken));
            given(tokenService.generateRefreshToken(user, DEVICE_INFO, IP_ADDRESS)).willReturn("new-refresh-token");
            given(tokenService.generateAccessToken(user)).willReturn("new-access-token");

            LoginResponse response = authService.refresh(REFRESH_TOKEN_RAW);

            assertThat(response.accessToken()).isEqualTo("new-access-token");
            assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        }

        @Test
        @DisplayName("deve revogar o token atual antes de gerar novos")
        void refresh_validToken_revokesOldToken() {
            User user = buildVerifiedUser();
            RefreshToken activeToken = buildActiveToken(user);
            assertThat(activeToken.isRevoked()).isFalse();

            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.of(activeToken));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn("new-rt");
            given(tokenService.generateAccessToken(any())).willReturn("new-at");

            authService.refresh(REFRESH_TOKEN_RAW);

            assertThat(activeToken.isRevoked()).isTrue();
        }

        @Test
        @DisplayName("deve registrar replacedByToken no token antigo")
        void refresh_validToken_marksOldTokenAsReplaced() {
            User user = buildVerifiedUser();
            RefreshToken activeToken = buildActiveToken(user);

            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.of(activeToken));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn("brand-new-refresh-token");
            given(tokenService.generateAccessToken(any())).willReturn("new-at");

            authService.refresh(REFRESH_TOKEN_RAW);

            assertThat(activeToken.getReplacedByToken()).isNotNull();
        }

        @Test
        @DisplayName("deve lançar InvalidTokenException quando token não existe")
        void refresh_tokenNotFound_throwsInvalidTokenException() {
            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidTokenException.class)
                    .isThrownBy(() -> authService.refresh(REFRESH_TOKEN_RAW));
        }

        @Test
        @DisplayName("deve revogar TODOS os tokens do usuário quando token já revogado (reuso detectado)")
        void refresh_revokedToken_revokesAllUserTokens() {
            User user = buildVerifiedUser();
            RefreshToken revokedToken = buildActiveToken(user);
            revokedToken.revoke();

            RefreshToken otherToken1 = buildActiveToken(user);
            RefreshToken otherToken2 = buildActiveToken(user);

            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.of(revokedToken));
            given(refreshTokenRepository.findAllByUser(user)).willReturn(List.of(otherToken1, otherToken2));

            assertThatExceptionOfType(InvalidTokenException.class)
                    .isThrownBy(() -> authService.refresh(REFRESH_TOKEN_RAW));

            assertThat(otherToken1.isRevoked()).isTrue();
            assertThat(otherToken2.isRevoked()).isTrue();
        }

        @Test
        @DisplayName("deve lançar InvalidTokenException quando token expirou")
        void refresh_expiredToken_throwsInvalidTokenException() {
            User user = buildVerifiedUser();
            RefreshToken expiredToken = mock(RefreshToken.class);

            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.of(expiredToken));
            given(expiredToken.getExpiresAt()).willReturn(Instant.now().minus(7, ChronoUnit.DAYS));

            assertThatExceptionOfType(InvalidTokenException.class)
                    .isThrownBy(() -> authService.refresh(REFRESH_TOKEN_RAW));

            then(tokenService).should(never()).generateAccessToken(any());
            then(tokenService).should(never()).generateRefreshToken(any(), any(), any());
        }
    }

    // --- VERIFY EMAIL ------------------------------------------------------------------

    @Nested
    @DisplayName("verifyEmail()")
    class VerifyEmail {

        @Test
        @DisplayName("deve verificar email com sucesso")
        void verifyEmail_validCode_completesWithoutException() {
            VerifyEmailRequest request = new VerifyEmailRequest(VALID_EMAIL, "123456");
            User user = buildPersistedUnverifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            willDoNothing().given(verificationService).validateCode(anyLong(), anyString());

            assertThatNoException().isThrownBy(() -> authService.verifyEmail(request));
            then(verificationService).should().validateCode(user.getId(), "123456");
        }

        @Test
        @DisplayName("deve lançar EmailAlreadyVerifiedException quando email já verificado")
        void verifyEmail_alreadyVerified_throwsEmailAlreadyVerifiedException() {
            VerifyEmailRequest request = new VerifyEmailRequest(VALID_EMAIL, "123456");
            User verifiedUser = buildVerifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(verifiedUser));

            assertThatExceptionOfType(EmailAlreadyVerifiedException.class)
                    .isThrownBy(() -> authService.verifyEmail(request));

            then(verificationService).should(never()).validateCode(anyLong(), anyString());
        }

        @Test
        @DisplayName("deve lançar InvalidCredentialsException quando usuário não encontrado")
        void verifyEmail_userNotFound_throwsInvalidCredentialsException() {
            VerifyEmailRequest request = new VerifyEmailRequest(VALID_EMAIL, "123456");
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> authService.verifyEmail(request));
        }

        @Test
        @DisplayName("deve propagar InvalidCodeException quando código inválido")
        void verifyEmail_invalidCode_propagatesInvalidCodeException() {
            VerifyEmailRequest request = new VerifyEmailRequest(VALID_EMAIL, "999999");
            User user = buildPersistedUnverifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            willThrow(new InvalidCodeException())
                    .given(verificationService).validateCode(anyLong(), eq("999999"));

            assertThatExceptionOfType(InvalidCodeException.class)
                    .isThrownBy(() -> authService.verifyEmail(request));
        }
    }

    // --- RESEND EMAIL ------------------------------------------------------------------

    @Nested
    @DisplayName("resendEmail()")
    class ResendEmail {

        @Test
        @DisplayName("deve reenviar email de verificação com sucesso")
        void resendEmail_existingUser_queuesEmail() {
            ResendEmailRequest request = new ResendEmailRequest(VALID_EMAIL);
            User user = buildPersistedUnverifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(verificationService.createCode(user))
                    .willReturn(new EmailVerificationCreationResult(null, "654321"));

            assertThatNoException().isThrownBy(() -> authService.resendEmail(request));

            ArgumentCaptor<EmailMessageRequest> captor =
                    ArgumentCaptor.forClass(EmailMessageRequest.class);
            then(producerService).should().producer(captor.capture());
            assertThat(captor.getValue().to()).isEqualTo(VALID_EMAIL);
        }

        @Test
        @DisplayName("deve lançar InvalidCredentialsException quando usuário não encontrado")
        void resendEmail_userNotFound_throwsInvalidCredentialsException() {
            ResendEmailRequest request = new ResendEmailRequest(VALID_EMAIL);
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> authService.resendEmail(request));

            then(producerService).should(never()).producer(any());
        }

        @Test
        @DisplayName("deve propagar RateLimitExceededException quando rate limit atingido")
        void resendEmail_rateLimitExceeded_propagatesRateLimitExceededException() {
            ResendEmailRequest request = new ResendEmailRequest(VALID_EMAIL);
            User user = buildPersistedUnverifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(verificationService.createCode(user)).willThrow(new RateLimitExceededException());

            assertThatExceptionOfType(RateLimitExceededException.class)
                    .isThrownBy(() -> authService.resendEmail(request));

            then(producerService).should(never()).producer(any());
        }

        @Test
        @DisplayName("deve reenviar para usuário já verificado sem verificar o status")
        void resendEmail_verifiedUser_stillSendsEmail() {
            ResendEmailRequest request = new ResendEmailRequest(VALID_EMAIL);
            User verifiedUser = buildVerifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(verifiedUser));
            given(verificationService.createCode(verifiedUser))
                    .willReturn(new EmailVerificationCreationResult(null, "111111"));

            assertThatNoException().isThrownBy(() -> authService.resendEmail(request));
            then(producerService).should().producer(any());
        }
    }

    // --- FORGOT PASSWORD ------------------------------------------------------------------

    @Nested
    @DisplayName("processForgotPassword()")
    class ProcessForgotPassword {

        @Test
        @DisplayName("deve gerar token e publicar email quando usuário existe e email verificado")
        void forgotPassword_verifiedUser_generatesTokenAndQueuesEmail() {
            ForgotPasswordRequest request = new ForgotPasswordRequest(VALID_EMAIL);
            User user = buildVerifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(user));
            given(passwordResetTokenService.generatePasswordResetToken(user, DEVICE_INFO, IP_ADDRESS))
                    .willReturn("raw-reset-token");

            assertThatNoException()
                    .isThrownBy(() -> authService.processForgotPassword(request, DEVICE_INFO, IP_ADDRESS));

            ArgumentCaptor<EmailMessageRequest> captor =
                    ArgumentCaptor.forClass(EmailMessageRequest.class);
            then(producerService).should().producer(captor.capture());
            assertThat(captor.getValue().to()).isEqualTo(VALID_EMAIL);
        }

        @Test
        @DisplayName("deve lançar EmailNotVerifiedException quando email não verificado")
        void forgotPassword_emailNotVerified_throwsEmailNotVerifiedException() {
            ForgotPasswordRequest request = new ForgotPasswordRequest(VALID_EMAIL);
            User unverifiedUser = buildPersistedUnverifiedUser();

            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.of(unverifiedUser));

            assertThatExceptionOfType(EmailNotVerifiedException.class)
                    .isThrownBy(() -> authService.processForgotPassword(request, DEVICE_INFO, IP_ADDRESS));

            then(producerService).should(never()).producer(any());
        }

        @Test
        @DisplayName("não deve lançar exceção quando usuário não encontrado (resposta silenciosa)")
        void forgotPassword_userNotFound_completesWithoutException() {
            ForgotPasswordRequest request = new ForgotPasswordRequest(VALID_EMAIL);
            given(userRepository.findByEmail(new Email(VALID_EMAIL))).willReturn(Optional.empty());

            assertThatNoException()
                    .isThrownBy(() -> authService.processForgotPassword(request, DEVICE_INFO, IP_ADDRESS));

            then(producerService).should(never()).producer(any());
            then(passwordResetTokenService).should(never()).generatePasswordResetToken(any(), any(), any());
        }
    }

    // --- RESET PASSWORD ------------------------------------------------------------------

    @Nested
    @DisplayName("resetPassword()")
    class ResetPassword {

        @Test
        @DisplayName("deve alterar senha com sucesso quando token válido e senhas coincidem")
        void resetPassword_validRequest_changesPasswordAndMarksTokenAsUsed() {
            UpdatePasswordRequest request = new UpdatePasswordRequest(
                    "valid-raw-token", "newPassword@123", "newPassword@123"
            );

            User user = buildVerifiedUser();
            var token = mock(com.javacore.spring_api_luvine.auth.domain.entity.PasswordResetToken.class);

            given(token.getExpiresAt()).willReturn(Instant.now().plus(1, ChronoUnit.HOURS));
            given(token.getUser()).willReturn(user);
            given(passwordResetTokenRepository.findByTokenAndUsedFalse(anyString()))
                    .willReturn(Optional.of(token));
            given(passwordEncoder.encode("newPassword@123")).willReturn("encoded-new-password");

            assertThatNoException().isThrownBy(() -> authService.resetPassword(request));

            then(token).should().markAsUsed();
            then(passwordResetTokenRepository).should().save(token);
        }

        @Test
        @DisplayName("deve lançar PasswordMisMatchException quando senhas não coincidem")
        void resetPassword_passwordMismatch_throwsPasswordMisMatchException() {
            UpdatePasswordRequest request = new UpdatePasswordRequest(
                    "valid-raw-token", "newPassword@123", "different@456"
            );

            assertThatExceptionOfType(PasswordMisMatchException.class)
                    .isThrownBy(() -> authService.resetPassword(request));

            then(passwordResetTokenRepository).should(never()).findByTokenAndUsedFalse(any());
        }

        @Test
        @DisplayName("deve lançar InvalidTokenException quando token não encontrado ou já usado")
        void resetPassword_tokenNotFound_throwsInvalidTokenException() {
            UpdatePasswordRequest request = new UpdatePasswordRequest(
                    "invalid-token", "newPassword@123", "newPassword@123"
            );

            given(passwordResetTokenRepository.findByTokenAndUsedFalse(anyString()))
                    .willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidTokenException.class)
                    .isThrownBy(() -> authService.resetPassword(request));
        }

        @Test
        @DisplayName("deve lançar InvalidTokenException quando token expirou")
        void resetPassword_expiredToken_throwsInvalidTokenException() {
            UpdatePasswordRequest request = new UpdatePasswordRequest(
                    "expired-token", "newPassword@123", "newPassword@123"
            );

            var expiredToken = mock(com.javacore.spring_api_luvine.auth.domain.entity.PasswordResetToken.class);
            given(expiredToken.getExpiresAt()).willReturn(Instant.now().minus(1, ChronoUnit.HOURS));
            given(passwordResetTokenRepository.findByTokenAndUsedFalse(anyString()))
                    .willReturn(Optional.of(expiredToken));

            assertThatExceptionOfType(InvalidTokenException.class)
                    .isThrownBy(() -> authService.resetPassword(request));

            then(expiredToken).should(never()).markAsUsed();
        }
    }

    // --- MÉTODO PRIVADO: findUserByEmailOrThrow ------------------------------------------------------------------

    @Nested
    @DisplayName("findUserByEmailOrThrow() — comportamento compartilhado")
    class FindUserByEmailOrThrow {

        @Test
        @DisplayName("deve normalizar email antes de buscar no repositório")
        void findUser_emailWithSpacesAndUpperCase_normalizesBeforeQuery() {
            LoginRequest request = new LoginRequest("  USER@EXAMPLE.COM  ", VALID_PASSWORD);

            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(new Email("  USER@EXAMPLE.COM  ")))
                    .willReturn(Optional.of(buildVerifiedUser()));
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN_RAW);

            authService.login(request, DEVICE_INFO, IP_ADDRESS);

            then(userRepository).should().findByEmail(new Email("  USER@EXAMPLE.COM  "));
        }
    }
}