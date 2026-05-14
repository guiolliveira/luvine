package com.javacore.spring_api_luvine.auth.service;

import com.javacore.spring_api_luvine.auth.domain.entity.RefreshToken;
import com.javacore.spring_api_luvine.auth.repository.RefreshTokenRepository;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenService")
class TokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private static final String EMAIL = "user@example.com";

    private static final String DEVICE_INFO =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36 Brave/136.0.0.0";

    private static final String IP_ADDRESS = "192.168.0.1";

    private static final String ACCESS_TOKEN =
            "header.payload.signature";

    private User mockAccessTokenUser() {
        User user = mock(User.class);

        given(user.getPublicId()).willReturn(UUID.randomUUID());
        given(user.getEmail()).willReturn(new Email(EMAIL));

        return user;
    }

    private User mockRefreshTokenUser() {
        return mock(User.class);
    }

    private Jwt mockJwt(String tokenValue) {
        return new Jwt(
                tokenValue,
                Instant.now(),
                Instant.now().plusSeconds(900),
                java.util.Map.of("alg", "RS256"),
                java.util.Map.of("sub", EMAIL)
        );
    }

    // --- GENERATE ACCESS TOKEN -------------------------------------------------

    @Nested
    @DisplayName("generateAccessToken()")
    class GenerateAccessToken {

        @Test
        @DisplayName("deve retornar o token gerado pelo JwtEncoder")
        void generateAccessToken_validUser_returnsEncodedTokenValue() {
            User user = mockAccessTokenUser();

            given(jwtEncoder.encode(any()))
                    .willReturn(mockJwt(ACCESS_TOKEN));

            String result = tokenService.generateAccessToken(user);

            assertThat(result)
                    .isEqualTo(ACCESS_TOKEN);
        }

        @Test
        @DisplayName("deve chamar JwtEncoder exatamente uma vez")
        void generateAccessToken_validUser_encodesExactlyOnce() {
            User user = mockAccessTokenUser();

            given(jwtEncoder.encode(any()))
                    .willReturn(mockJwt(ACCESS_TOKEN));

            tokenService.generateAccessToken(user);

            then(jwtEncoder)
                    .should(times(1))
                    .encode(any(JwtEncoderParameters.class));
        }

        @Test
        @DisplayName("deve usar email como subject")
        void generateAccessToken_validUser_usesEmailAsSubject() {
            User user = mockAccessTokenUser();

            ArgumentCaptor<JwtEncoderParameters> captor =
                    ArgumentCaptor.forClass(JwtEncoderParameters.class);

            given(jwtEncoder.encode(captor.capture()))
                    .willReturn(mockJwt(ACCESS_TOKEN));

            tokenService.generateAccessToken(user);

            assertThat(captor.getValue().getClaims().getSubject())
                    .isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("deve incluir claims obrigatórios")
        void generateAccessToken_validUser_containsRequiredClaims() {
            User user = mockAccessTokenUser();

            UUID publicId = user.getPublicId();

            ArgumentCaptor<JwtEncoderParameters> captor =
                    ArgumentCaptor.forClass(JwtEncoderParameters.class);

            given(jwtEncoder.encode(captor.capture()))
                    .willReturn(mockJwt(ACCESS_TOKEN));

            tokenService.generateAccessToken(user);

            var claims = captor.getValue().getClaims();

            String claimPublicId = claims.getClaim("publicId");
            String type = claims.getClaim("type");
            String jti = claims.getClaim("jti");

            assertThat(claimPublicId)
                    .isEqualTo(publicId.toString());

            assertThat(type)
                    .isEqualTo("access");

            assertThat(jti)
                    .isNotBlank();

            assertThat(UUID.fromString(jti))
                    .isNotNull();

            assertThat(claims.<String>getClaim("iss"))
                    .isEqualTo("Api-Luvine");
        }

        @Test
        @DisplayName("deve definir expiresAt após issuedAt")
        void generateAccessToken_validUser_expiresAfterIssuedAt() {
            User user = mockAccessTokenUser();

            ArgumentCaptor<JwtEncoderParameters> captor =
                    ArgumentCaptor.forClass(JwtEncoderParameters.class);

            given(jwtEncoder.encode(captor.capture()))
                    .willReturn(mockJwt(ACCESS_TOKEN));

            tokenService.generateAccessToken(user);

            Instant issuedAt =
                    captor.getValue().getClaims().getIssuedAt();

            Instant expiresAt =
                    captor.getValue().getClaims().getExpiresAt();

            assertThat(expiresAt)
                    .isAfter(issuedAt);
        }
    }

    // --- GENERATE REFRESH TOKEN ------------------------------------------------

    @Nested
    @DisplayName("generateRefreshToken()")
    class GenerateRefreshToken {

        @Test
        @DisplayName("deve retornar token raw não vazio")
        void generateRefreshToken_validArgs_returnsNonBlankToken() {
            User user = mockRefreshTokenUser();

            String token =
                    tokenService.generateRefreshToken(
                            user,
                            DEVICE_INFO,
                            IP_ADDRESS
                    );

            assertThat(token)
                    .isNotBlank();
        }

        @Test
        @DisplayName("deve gerar tokens diferentes a cada chamada")
        void generateRefreshToken_calledTwice_returnsDistinctTokens() {
            User user = mockRefreshTokenUser();

            String first =
                    tokenService.generateRefreshToken(
                            user,
                            DEVICE_INFO,
                            IP_ADDRESS
                    );

            String second =
                    tokenService.generateRefreshToken(
                            user,
                            DEVICE_INFO,
                            IP_ADDRESS
                    );

            assertThat(first)
                    .isNotEqualTo(second);
        }

        @Test
        @DisplayName("deve persistir refresh token")
        void generateRefreshToken_validArgs_savesToken() {
            User user = mockRefreshTokenUser();

            tokenService.generateRefreshToken(
                    user,
                    DEVICE_INFO,
                    IP_ADDRESS
            );

            then(refreshTokenRepository)
                    .should()
                    .save(any());
        }

        @Test
        @DisplayName("deve truncar deviceInfo para 255")
        void generateRefreshToken_longDeviceInfo_truncates() {
            User user = mockRefreshTokenUser();

            String device =
                    "A".repeat(300);

            ArgumentCaptor<RefreshToken> captor =
                    ArgumentCaptor.forClass(RefreshToken.class);

            tokenService.generateRefreshToken(
                    user,
                    device,
                    IP_ADDRESS
            );

            then(refreshTokenRepository)
                    .should()
                    .save(captor.capture());

            assertThat(captor.getValue().getDeviceInfo())
                    .hasSize(255);
        }

        @Test
        @DisplayName("deve usar apenas o primeiro IP")
        void generateRefreshToken_multipleIps_usesFirstOnly() {
            User user = mockRefreshTokenUser();

            ArgumentCaptor<RefreshToken> captor =
                    ArgumentCaptor.forClass(RefreshToken.class);

            tokenService.generateRefreshToken(
                    user,
                    DEVICE_INFO,
                    "10.0.0.1, 172.16.0.1, 192.168.0.1"
            );

            then(refreshTokenRepository)
                    .should()
                    .save(captor.capture());

            assertThat(captor.getValue().getIpAddress())
                    .isEqualTo("10.0.0.1");
        }

        @Test
        @DisplayName("deve aceitar deviceInfo nulo")
        void generateRefreshToken_nullDeviceInfo_doesNotThrow() {
            User user = mockRefreshTokenUser();

            assertThatNoException()
                    .isThrownBy(() ->
                            tokenService.generateRefreshToken(
                                    user,
                                    null,
                                    IP_ADDRESS
                            ));
        }

        @Test
        @DisplayName("deve aceitar ipAddress nulo")
        void generateRefreshToken_nullIpAddress_doesNotThrow() {
            User user = mockRefreshTokenUser();

            assertThatNoException()
                    .isThrownBy(() ->
                            tokenService.generateRefreshToken(
                                    user,
                                    DEVICE_INFO,
                                    null
                            ));
        }

        @Test
        @DisplayName("deve salvar hash e não token raw")
        void generateRefreshToken_validArgs_savesHashNotRaw() {
            User user = mockRefreshTokenUser();

            ArgumentCaptor<RefreshToken> captor =
                    ArgumentCaptor.forClass(RefreshToken.class);

            String raw =
                    tokenService.generateRefreshToken(
                            user,
                            DEVICE_INFO,
                            IP_ADDRESS
                    );

            then(refreshTokenRepository)
                    .should()
                    .save(captor.capture());

            assertThat(captor.getValue().getToken())
                    .isNotEqualTo(raw);
        }
    }

    // --- CLEAN --------------------------------------------------------------

    @Nested
    @DisplayName("cleanExpiresTokens()")
    class CleanExpiresTokens {

        @Test
        @DisplayName("deve limpar tokens expirados")
        void cleanExpiresTokens_always_callsRepository() {
            tokenService.cleanExpiresTokens();

            then(refreshTokenRepository)
                    .should()
                    .deleteInvalidTokens(any(Instant.class));
        }
    }
}