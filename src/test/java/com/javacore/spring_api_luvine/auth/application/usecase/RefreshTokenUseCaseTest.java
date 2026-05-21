package com.javacore.spring_api_luvine.auth.application.usecase;

import com.javacore.spring_api_luvine.auth.application.dto.LoginResponse;
import com.javacore.spring_api_luvine.auth.application.dto.RefreshTokenRequest;
import com.javacore.spring_api_luvine.auth.application.service.TokenService;
import com.javacore.spring_api_luvine.auth.domain.entity.RefreshToken;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidTokenException;
import com.javacore.spring_api_luvine.auth.infrastructure.repository.RefreshTokenRepository;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("RefreshTokenUseCase")
@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock private TokenService tokenService;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenUseCase refreshTokenUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final String DEVICE_INFO        = "Mozilla/5.0";
    private static final String IP_ADDRESS         = "192.168.0.1";
    private static final String RAW_REFRESH_TOKEN  = "raw-refresh-token-value";
    private static final String NEW_ACCESS_TOKEN   = "new-access-token";
    private static final String NEW_REFRESH_TOKEN  = "new-refresh-token";

    private User buildVerifiedUser() {
        User user = User.create(
                new Email("user@example.com"),
                new PersonName("User"),
                new PersonName("Name"),
                new Password("Password@123"),
                UserProvider.LOCAL
        );
        user.markEmailAsVerified();
        return user;
    }

    private RefreshToken buildActiveToken(User user) {
        return RefreshToken.create(user, "hashed-token", DEVICE_INFO, IP_ADDRESS);
    }

    private RefreshTokenRequest validRequest() {
        return new RefreshTokenRequest(RAW_REFRESH_TOKEN);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve retornar novos tokens quando refresh token é válido")
        void execute_validToken_returnsNewTokenPair() {
            User user = buildVerifiedUser();
            RefreshToken activeToken = buildActiveToken(user);

            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.of(activeToken));
            given(tokenService.generateRefreshToken(user, DEVICE_INFO, IP_ADDRESS)).willReturn(NEW_REFRESH_TOKEN);
            given(tokenService.generateAccessToken(user)).willReturn(NEW_ACCESS_TOKEN);

            LoginResponse response = refreshTokenUseCase.execute(validRequest());

            assertThat(response.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);
        }

        @Test
        @DisplayName("deve revogar o token atual antes de gerar novos")
        void execute_validToken_revokesOldToken() {
            User user = buildVerifiedUser();
            RefreshToken activeToken = buildActiveToken(user);
            assertThat(activeToken.isRevoked()).isFalse();

            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.of(activeToken));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(NEW_REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(NEW_ACCESS_TOKEN);

            refreshTokenUseCase.execute(validRequest());

            assertThat(activeToken.isRevoked()).isTrue();
        }

        @Test
        @DisplayName("deve registrar replacedByToken no token antigo")
        void execute_validToken_marksOldTokenAsReplaced() {
            User user = buildVerifiedUser();
            RefreshToken activeToken = buildActiveToken(user);

            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.of(activeToken));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(NEW_REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(NEW_ACCESS_TOKEN);

            refreshTokenUseCase.execute(validRequest());

            assertThat(activeToken.getReplacedByToken()).isNotNull();
        }

        @Test
        @DisplayName("deve lançar InvalidTokenException quando token não encontrado")
        void execute_tokenNotFound_throwsInvalidTokenException() {
            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidTokenException.class)
                    .isThrownBy(() -> refreshTokenUseCase.execute(validRequest()));

            then(tokenService).should(never()).generateAccessToken(any());
            then(tokenService).should(never()).generateRefreshToken(any(), any(), any());
        }

        @Test
        @DisplayName("deve lançar InvalidTokenException quando token expirou")
        void execute_expiredToken_throwsInvalidTokenException() {
            User user = buildVerifiedUser();
            RefreshToken expiredToken = mock(RefreshToken.class);

            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.of(expiredToken));
            given(expiredToken.getExpiresAt()).willReturn(Instant.now().minus(7, ChronoUnit.DAYS));

            assertThatExceptionOfType(InvalidTokenException.class)
                    .isThrownBy(() -> refreshTokenUseCase.execute(validRequest()));

            then(tokenService).should(never()).generateAccessToken(any());
            then(tokenService).should(never()).generateRefreshToken(any(), any(), any());
        }

        @Test
        @DisplayName("deve revogar TODOS os tokens do usuário quando token já revogado (reuso detectado)")
        void execute_revokedToken_revokesAllUserTokens() {
            User user = buildVerifiedUser();
            RefreshToken revokedToken = buildActiveToken(user);
            revokedToken.revoke();

            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.of(revokedToken));

            assertThatExceptionOfType(InvalidTokenException.class)
                    .isThrownBy(() -> refreshTokenUseCase.execute(validRequest()));

            then(refreshTokenRepository).should().revokeAllUserTokens(user);
            then(tokenService).should(never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("não deve chamar revokeAllUserTokens quando token é válido")
        void execute_validToken_doesNotRevokeAllTokens() {
            User user = buildVerifiedUser();
            RefreshToken activeToken = buildActiveToken(user);

            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.of(activeToken));
            given(tokenService.generateRefreshToken(any(), any(), any())).willReturn(NEW_REFRESH_TOKEN);
            given(tokenService.generateAccessToken(any())).willReturn(NEW_ACCESS_TOKEN);

            refreshTokenUseCase.execute(validRequest());

            then(refreshTokenRepository).should(never()).revokeAllUserTokens(any());
        }
    }
}