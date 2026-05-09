package com.javacore.spring_api_luvine.auth.service;

import com.javacore.spring_api_luvine.auth.domain.entity.RefreshToken;
import com.javacore.spring_api_luvine.auth.domain.exception.*;
import com.javacore.spring_api_luvine.auth.dto.*;
import com.javacore.spring_api_luvine.auth.mapper.AuthMapper;
import com.javacore.spring_api_luvine.auth.repository.RefreshTokenRepository;
import com.javacore.spring_api_luvine.shared.messaging.dto.EmailMessageRequest;
import com.javacore.spring_api_luvine.shared.messaging.service.producer.ProducerService;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
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

    @Mock
    private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthMapper authMapper;
    @Mock private TokenService tokenService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private EmailVerificationService verificationService;
    @Mock private ProducerService producerService;

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

    private User buildVerifiedUser() {
        User user = User.create(
                VALID_EMAIL,
                VALID_FIRST_NAME,
                VALID_LAST_NAME,
                "encoded-password",
                UserProvider.LOCAL
        );
        user.markEmailAsVerified();
        return user;
    }

    private User buildPersistedUnverifiedUser() {
        User user = User.create(
                VALID_EMAIL,
                VALID_FIRST_NAME,
                VALID_LAST_NAME,
                "encoded-password",
                UserProvider.LOCAL
        );

        ReflectionTestUtils.setField(user, "id", 1L);

        return user;
    }

    private RegisterRequest validRegisterRequest() {
        return new RegisterRequest(
                VALID_EMAIL,
                VALID_FIRST_NAME,
                VALID_LAST_NAME,
                VALID_PASSWORD,
                VALID_PASSWORD
        );
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

            given(userRepository.existsByEmail(VALID_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(VALID_PASSWORD)).willReturn("encoded-password");
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            EmailVerificationCreationResult verificationResult =
                    new EmailVerificationCreationResult(null, "123456");
            given(verificationService.createCode(any(User.class))).willReturn(verificationResult);

            RegisterResponse expectedResponse = new RegisterResponse(
                    UUID.randomUUID(), VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME,
                    Instant.now(), true
            );
            given(authMapper.toRegisterResponse(any(User.class))).willReturn(expectedResponse);

            RegisterResponse response = authService.register(request);

            assertThat(response).isNotNull();
            assertThat(response).isEqualTo(expectedResponse);

            then(userRepository).should().save(any(User.class));

            ArgumentCaptor<EmailMessageRequest> emailCaptor =
                    ArgumentCaptor.forClass(EmailMessageRequest.class);
            then(producerService).should().producer(emailCaptor.capture());

            EmailMessageRequest publishedEmail = emailCaptor.getValue();
            assertThat(publishedEmail.to()).isEqualTo(VALID_EMAIL);
            assertThat(publishedEmail.body()).isEqualTo("123456");
        }

        @Test
        @DisplayName("deve lançar EmailAlreadyExistsException quando email já cadastrado")
        void register_emailAlreadyExists_throwsEmailAlreadyExistsException() {
            RegisterRequest request = validRegisterRequest();
            given(userRepository.existsByEmail(VALID_EMAIL)).willReturn(true);

            assertThatExceptionOfType(EmailAlreadyExistsException.class)
                    .isThrownBy(() -> authService.register(request));

            then(userRepository).should(never()).save(any());
            then(producerService).should(never()).producer(any());
        }

        @Test
        @DisplayName("deve lançar PasswordMisMatchException quando senhas não coincidem")
        void register_passwordMismatch_throwsPasswordMisMatchException() {
            RegisterRequest request = new RegisterRequest(
                    VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME,
                    "password@123", "password@456"
            );
            given(userRepository.existsByEmail(VALID_EMAIL)).willReturn(false);

            assertThatExceptionOfType(PasswordMisMatchException.class)
                    .isThrownBy(() -> authService.register(request));

            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar InvalidEmailException quando email é inválido")
        void register_invalidEmail_throwsInvalidEmailException() {
            RegisterRequest request = new RegisterRequest(
                    "not-an-email", VALID_FIRST_NAME, VALID_LAST_NAME,
                    VALID_PASSWORD, VALID_PASSWORD
            );

            assertThatException()
                    .isThrownBy(() -> authService.register(request));

            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve normalizar email para lowercase antes de persistir")
        void register_emailWithUpperCase_normalizesToLowerCase() {
            RegisterRequest request = new RegisterRequest(
                    "USER@EXAMPLE.COM", VALID_FIRST_NAME, VALID_LAST_NAME,
                    VALID_PASSWORD, VALID_PASSWORD
            );

            given(userRepository.existsByEmail("user@example.com")).willReturn(false);
            given(passwordEncoder.encode(any())).willReturn("encoded");
            given(verificationService.createCode(any())).willReturn(
                    new EmailVerificationCreationResult(null, "000000"));
            given(authMapper.toRegisterResponse(any())).willReturn(
                    new RegisterResponse(UUID.randomUUID(), "user@example.com",
                            VALID_FIRST_NAME, VALID_LAST_NAME, Instant.now(), true));

            authService.register(request);

            then(userRepository).should().existsByEmail("user@example.com");
        }

        @Test
        @DisplayName("não deve publicar email quando verificationService lança exceção")
        void register_verificationServiceThrows_doesNotPublishEmail() {
            RegisterRequest request = validRegisterRequest();
            given(userRepository.existsByEmail(VALID_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(any())).willReturn("encoded");
            given(verificationService.createCode(any()))
                    .willThrow(new RateLimitExceededException());

            assertThatExceptionOfType(RateLimitExceededException.class)
                    .isThrownBy(() -> authService.register(request));

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
            LoginRequest request = validLoginRequest();
            User user = buildVerifiedUser();

            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.of(user));
            given(tokenService.generateRefreshToken(user, DEVICE_INFO, IP_ADDRESS))
                    .willReturn(REFRESH_TOKEN_RAW);
            given(tokenService.generateAccessToken(user)).willReturn(ACCESS_TOKEN);

            LoginResponse response = authService.login(request, DEVICE_INFO, IP_ADDRESS);

            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN_RAW);
        }

        @Test
        @DisplayName("deve lançar InvalidCredentialsException quando autenticação falha")
        void login_badCredentials_throwsInvalidCredentialsException() {
            LoginRequest request = validLoginRequest();
            given(authenticationManager.authenticate(any()))
                    .willThrow(new BadCredentialsException("bad credentials"));

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> authService.login(request, DEVICE_INFO, IP_ADDRESS));

            then(tokenService).should(never()).generateAccessToken(any());
            then(tokenService).should(never()).generateRefreshToken(any(), any(), any());
        }

        @Test
        @DisplayName("deve lançar EmailNotVerifiedException quando email não verificado")
        void login_emailNotVerified_throwsEmailNotVerifiedException() {
            LoginRequest request = validLoginRequest();
            User unverifiedUser = buildPersistedUnverifiedUser();

            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.of(unverifiedUser));

            assertThatExceptionOfType(EmailNotVerifiedException.class)
                    .isThrownBy(() -> authService.login(request, DEVICE_INFO, IP_ADDRESS));

            then(tokenService).should(never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("deve lançar ProviderConflictException quando usuário é OAuth (não LOCAL)")
        void login_oauthUser_throwsProviderConflictException() {
            LoginRequest request = validLoginRequest();
            User oauthUser = User.create(
                    VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME,
                    "", UserProvider.GOOGLE
            );
            oauthUser.markEmailAsVerified();

            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.of(oauthUser));

            assertThatExceptionOfType(ProviderConflictException.class)
                    .isThrownBy(() -> authService.login(request, DEVICE_INFO, IP_ADDRESS));
        }

        @Test
        @DisplayName("deve lançar InvalidCredentialsException quando usuário não encontrado")
        void login_userNotFound_throwsInvalidCredentialsException() {
            LoginRequest request = validLoginRequest();
            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> authService.login(request, DEVICE_INFO, IP_ADDRESS));
        }

        @Test
        @DisplayName("deve passar o authenticationToken correto para o AuthenticationManager")
        void login_validRequest_authenticatesWithCorrectToken() {
            LoginRequest request = validLoginRequest();
            User user = buildVerifiedUser();

            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.of(user));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN_RAW);
            given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);

            authService.login(request, DEVICE_INFO, IP_ADDRESS);

            ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            then(authenticationManager).should().authenticate(authCaptor.capture());

            assertThat(authCaptor.getValue().getPrincipal()).isEqualTo(VALID_EMAIL);
            assertThat(authCaptor.getValue().getCredentials()).isEqualTo(VALID_PASSWORD);
        }

        @Test
        @DisplayName("deve aceitar deviceInfo e ipAddress nulos sem lançar exceção")
        void login_nullDeviceAndIp_doesNotThrow() {
            LoginRequest request = validLoginRequest();
            User user = buildVerifiedUser();

            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.of(user));
            given(tokenService.generateRefreshToken(user, null, null)).willReturn(REFRESH_TOKEN_RAW);
            given(tokenService.generateAccessToken(user)).willReturn(ACCESS_TOKEN);

            assertThatNoException()
                    .isThrownBy(() -> authService.login(request, null, null));
        }
    }

    // --- REFRESH ------------------------------------------------------------------

    @Nested
    @DisplayName("refresh()")
    class Refresh {

        private RefreshToken buildActiveToken(User user) {
            return RefreshToken.create(
                    user,
                    "hashed-token",
                    DEVICE_INFO,
                    IP_ADDRESS
            );
        }

        @Test
        @DisplayName("deve retornar novos tokens quando refresh token é válido")
        void refresh_validToken_returnsNewTokenPair() {
            User user = buildVerifiedUser();
            RefreshToken activeToken = buildActiveToken(user);

            given(refreshTokenRepository.findByToken(anyString()))
                    .willReturn(Optional.of(activeToken));
            given(tokenService.generateRefreshToken(user, DEVICE_INFO, IP_ADDRESS))
                    .willReturn("new-refresh-token");
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

            given(refreshTokenRepository.findByToken(anyString()))
                    .willReturn(Optional.of(activeToken));
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

            given(refreshTokenRepository.findByToken(anyString()))
                    .willReturn(Optional.of(activeToken));
            given(tokenService.generateRefreshToken(any(), any(), any()))
                    .willReturn("brand-new-refresh-token");
            given(tokenService.generateAccessToken(any())).willReturn("new-at");

            authService.refresh(REFRESH_TOKEN_RAW);

            assertThat(activeToken.getReplacedByToken()).isNotNull();
        }

        @Test
        @DisplayName("deve lançar InvalidRefreshTokenException quando token não existe")
        void refresh_tokenNotFound_throwsInvalidRefreshTokenException() {
            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidRefreshTokenException.class)
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

            given(refreshTokenRepository.findByToken(anyString()))
                    .willReturn(Optional.of(revokedToken));
            given(refreshTokenRepository.findAllByUser(user))
                    .willReturn(List.of(otherToken1, otherToken2));

            assertThatExceptionOfType(InvalidRefreshTokenException.class)
                    .isThrownBy(() -> authService.refresh(REFRESH_TOKEN_RAW));

            assertThat(otherToken1.isRevoked()).isTrue();
            assertThat(otherToken2.isRevoked()).isTrue();
        }

        @Test
        @DisplayName("deve lançar InvalidRefreshTokenException quando token expirou")
        void refresh_expiredToken_throwsInvalidRefreshTokenException() {
            User user = buildVerifiedUser();
            RefreshToken expiredToken = mock(RefreshToken.class);

            given(refreshTokenRepository.findByToken(anyString()))
                    .willReturn(Optional.of(expiredToken));
            given(expiredToken.isRevoked()).willReturn(false);
            given(expiredToken.getExpiresAt()).willReturn(Instant.now().minus(7, ChronoUnit.DAYS));
            given(expiredToken.getUser()).willReturn(user);

            assertThatExceptionOfType(InvalidRefreshTokenException.class)
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
        @DisplayName("deve verificar email com sucesso e retornar mensagem")
        void verifyEmail_validCode_returnsSuccessMessage() {
            VerifyEmailRequest request = new VerifyEmailRequest(VALID_EMAIL, "123456");
            User user = buildPersistedUnverifiedUser();

            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.of(user));
            willDoNothing().given(verificationService).validateCode(anyLong(), anyString());

            MessageResponse response = authService.verifyEmail(request);

            assertThat(response.message()).isEqualTo("Email verificado com sucesso!");
            then(verificationService).should().validateCode(user.getId(), "123456");
        }

        @Test
        @DisplayName("deve lançar EmailAlreadyVerifiedException quando email já verificado")
        void verifyEmail_alreadyVerified_throwsEmailAlreadyVerifiedException() {
            VerifyEmailRequest request = new VerifyEmailRequest(VALID_EMAIL, "123456");
            User verifiedUser = buildVerifiedUser();

            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.of(verifiedUser));

            assertThatExceptionOfType(EmailAlreadyVerifiedException.class)
                    .isThrownBy(() -> authService.verifyEmail(request));

            then(verificationService).should(never()).validateCode(anyLong(), anyString());
        }

        @Test
        @DisplayName("deve lançar InvalidCredentialsException quando usuário não encontrado")
        void verifyEmail_userNotFound_throwsInvalidCredentialsException() {
            VerifyEmailRequest request = new VerifyEmailRequest(VALID_EMAIL, "123456");
            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> authService.verifyEmail(request));
        }

        @Test
        @DisplayName("deve propagar InvalidCodeException quando código inválido")
        void verifyEmail_invalidCode_propagatesInvalidCodeException() {
            VerifyEmailRequest request = new VerifyEmailRequest(VALID_EMAIL, "999999");
            User user = buildPersistedUnverifiedUser();

            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.of(user));
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
        void resendEmail_existingUser_queuesEmailAndReturnsMessage() {
            ResendEmailRequest request = new ResendEmailRequest(VALID_EMAIL);
            User user = buildPersistedUnverifiedUser();

            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.of(user));
            given(verificationService.createCode(user))
                    .willReturn(new EmailVerificationCreationResult(null, "654321"));

            MessageResponse response = authService.resendEmail(request);

            assertThat(response.message()).isEqualTo("Email de verificação reenviado com sucesso!");

            ArgumentCaptor<EmailMessageRequest> captor =
                    ArgumentCaptor.forClass(EmailMessageRequest.class);
            then(producerService).should().producer(captor.capture());

            EmailMessageRequest sentEmail = captor.getValue();
            assertThat(sentEmail.to()).isEqualTo(VALID_EMAIL);
            assertThat(sentEmail.body()).isEqualTo("654321");
        }

        @Test
        @DisplayName("deve lançar InvalidCredentialsException quando usuário não encontrado")
        void resendEmail_userNotFound_throwsInvalidCredentialsException() {
            ResendEmailRequest request = new ResendEmailRequest(VALID_EMAIL);
            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> authService.resendEmail(request));

            then(producerService).should(never()).producer(any());
        }

        @Test
        @DisplayName("deve propagar RateLimitExceededException quando rate limit atingido")
        void resendEmail_rateLimitExceeded_propagatesRateLimitExceededException() {
            ResendEmailRequest request = new ResendEmailRequest(VALID_EMAIL);
            User user = buildPersistedUnverifiedUser();

            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.of(user));
            given(verificationService.createCode(user))
                    .willThrow(new RateLimitExceededException());

            assertThatExceptionOfType(RateLimitExceededException.class)
                    .isThrownBy(() -> authService.resendEmail(request));

            then(producerService).should(never()).producer(any());
        }

        @Test
        @DisplayName("deve reenviar para usuário já verificado sem verificar o status")
        void resendEmail_verifiedUser_stillSendsEmail() {
            ResendEmailRequest request = new ResendEmailRequest(VALID_EMAIL);
            User verifiedUser = buildVerifiedUser();

            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.of(verifiedUser));
            given(verificationService.createCode(verifiedUser))
                    .willReturn(new EmailVerificationCreationResult(null, "111111"));

            MessageResponse response = authService.resendEmail(request);

            assertThat(response).isNotNull();
            then(producerService).should().producer(any());
        }

        // --- METHODS PRIVATES ------------------------------------------------------------------

        @Nested
        @DisplayName("findUserByEmailOrThrow() — comportamento compartilhado")
        class FindUserByEmailOrThrow {

            @Test
            @DisplayName("deve normalizar email (trim + lowercase) antes de buscar no repositório")
            void findUser_emailWithSpacesAndUpperCase_normalizesBeforeQuery() {

                LoginRequest request = new LoginRequest("  USER@EXAMPLE.COM  ", VALID_PASSWORD);

                given(authenticationManager.authenticate(any())).willReturn(null);
                given(userRepository.findByEmail("user@example.com"))
                        .willReturn(Optional.of(buildVerifiedUser()));
                given(tokenService.generateAccessToken(any())).willReturn(ACCESS_TOKEN);
                given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(REFRESH_TOKEN_RAW);

                authService.login(request, DEVICE_INFO, IP_ADDRESS);

                then(userRepository).should().findByEmail("user@example.com");
            }
        }
    }
}