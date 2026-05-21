package com.javacore.spring_api_luvine.common.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javacore.spring_api_luvine.auth.application.dto.LoginResponse;
import com.javacore.spring_api_luvine.auth.application.usecase.OauthLoginUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@DisplayName("OauthAuthenticationSuccessHandler")
@ExtendWith(MockitoExtension.class)
class OauthAuthenticationSuccessHandlerTest {

    @Mock private OauthLoginUseCase oauthLoginUseCase;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private OauthAuthenticationSuccessHandler handler;

    // --- HELPERS ------------------------------------------------------------------

    private static final String VALID_EMAIL   = "user@example.com";
    private static final String VALID_NAME    = "João Silva";
    private static final String DEVICE_INFO   = "Mozilla/5.0";
    private static final String IP_ADDRESS    = "192.168.0.1";
    private static final String ACCESS_TOKEN  = "header.payload.signature";
    private static final String REFRESH_TOKEN = "raw-refresh-token-value";

    private OAuth2AuthenticationToken buildOauth2Token(String email, String name) {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        given(oAuth2User.getAttribute("email")).willReturn(email);
        given(oAuth2User.getAttribute("name")).willReturn(name);

        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        given(token.getPrincipal()).willReturn(oAuth2User);
        given(token.getAuthorizedClientRegistrationId()).willReturn("google");
        return token;
    }

    private HttpServletRequest buildRequest(String userAgent, String forwardedFor, String remoteAddr) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("User-Agent")).willReturn(userAgent);
        given(request.getHeader("X-Forwarded-For")).willReturn(forwardedFor);
        if (forwardedFor == null) {
            given(request.getRemoteAddr()).willReturn(remoteAddr);
        }
        return request;
    }

    private HttpServletResponse buildWritableResponse() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        given(response.getWriter()).willReturn(new PrintWriter(new StringWriter()));
        return response;
    }

    // --- ON AUTHENTICATION SUCCESS ------------------------------------------------------------------

    @Nested
    @DisplayName("onAuthenticationSuccess()")
    class OnAuthenticationSuccess {

        // --- FLUXO FELIZ ------------------------------------------------------------------

        @Test
        @DisplayName("deve chamar oauthLoginUseCase com email, nome, deviceInfo e ip corretos")
        void success_validOauth2Token_callsUseCaseWithCorrectArgs() throws Exception {
            OAuth2AuthenticationToken auth = buildOauth2Token(VALID_EMAIL, VALID_NAME);
            HttpServletRequest request = buildRequest(DEVICE_INFO, null, IP_ADDRESS);
            HttpServletResponse response = buildWritableResponse();

            given(oauthLoginUseCase.execute(VALID_EMAIL, VALID_NAME, DEVICE_INFO, IP_ADDRESS))
                    .willReturn(new LoginResponse(ACCESS_TOKEN, REFRESH_TOKEN));
            given(objectMapper.writeValueAsString(any())).willReturn("{\"accessToken\":\"" + ACCESS_TOKEN + "\"}");

            handler.onAuthenticationSuccess(request, response, auth);

            then(oauthLoginUseCase).should().execute(VALID_EMAIL, VALID_NAME, DEVICE_INFO, IP_ADDRESS);
        }

        @Test
        @DisplayName("deve usar X-Forwarded-For como ip quando header presente")
        void success_xForwardedForPresent_usesForwardedIp() throws Exception {
            OAuth2AuthenticationToken auth = buildOauth2Token(VALID_EMAIL, VALID_NAME);
            HttpServletRequest request = buildRequest(DEVICE_INFO, "10.0.0.1", null);
            HttpServletResponse response = buildWritableResponse();

            given(oauthLoginUseCase.execute(VALID_EMAIL, VALID_NAME, DEVICE_INFO, "10.0.0.1"))
                    .willReturn(new LoginResponse(ACCESS_TOKEN, REFRESH_TOKEN));
            given(objectMapper.writeValueAsString(any())).willReturn("{}");

            handler.onAuthenticationSuccess(request, response, auth);

            then(oauthLoginUseCase).should().execute(VALID_EMAIL, VALID_NAME, DEVICE_INFO, "10.0.0.1");
            then(request).should(never()).getRemoteAddr();
        }

        @Test
        @DisplayName("deve usar remoteAddr como ip quando X-Forwarded-For está ausente")
        void success_noXForwardedFor_usesRemoteAddr() throws Exception {
            OAuth2AuthenticationToken auth = buildOauth2Token(VALID_EMAIL, VALID_NAME);
            HttpServletRequest request = buildRequest(DEVICE_INFO, null, IP_ADDRESS);
            HttpServletResponse response = buildWritableResponse();

            given(oauthLoginUseCase.execute(VALID_EMAIL, VALID_NAME, DEVICE_INFO, IP_ADDRESS))
                    .willReturn(new LoginResponse(ACCESS_TOKEN, REFRESH_TOKEN));
            given(objectMapper.writeValueAsString(any())).willReturn("{}");

            handler.onAuthenticationSuccess(request, response, auth);

            then(oauthLoginUseCase).should().execute(VALID_EMAIL, VALID_NAME, DEVICE_INFO, IP_ADDRESS);
        }

        @Test
        @DisplayName("deve definir cookie refreshToken httpOnly e secure na resposta")
        void success_validOauth2Token_setsRefreshTokenCookie() throws Exception {
            OAuth2AuthenticationToken auth = buildOauth2Token(VALID_EMAIL, VALID_NAME);
            HttpServletRequest request = buildRequest(DEVICE_INFO, null, IP_ADDRESS);
            HttpServletResponse response = buildWritableResponse();

            given(oauthLoginUseCase.execute(any(), any(), any(), any()))
                    .willReturn(new LoginResponse(ACCESS_TOKEN, REFRESH_TOKEN));
            given(objectMapper.writeValueAsString(any())).willReturn("{}");

            handler.onAuthenticationSuccess(request, response, auth);

            ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);
            then(response).should().setHeader(eq("Set-Cookie"), cookieCaptor.capture());

            String cookie = cookieCaptor.getValue();
            assertThat(cookie).contains("refreshToken=" + REFRESH_TOKEN);
            assertThat(cookie).containsIgnoringCase("HttpOnly");
            assertThat(cookie).containsIgnoringCase("Secure");
        }

        @Test
        @DisplayName("deve retornar accessToken no body e null para refreshToken")
        void success_validOauth2Token_writesAccessTokenWithNullRefreshToken() throws Exception {
            OAuth2AuthenticationToken auth = buildOauth2Token(VALID_EMAIL, VALID_NAME);
            HttpServletRequest request = buildRequest(DEVICE_INFO, null, IP_ADDRESS);
            HttpServletResponse response = buildWritableResponse();

            given(oauthLoginUseCase.execute(any(), any(), any(), any()))
                    .willReturn(new LoginResponse(ACCESS_TOKEN, REFRESH_TOKEN));
            given(objectMapper.writeValueAsString(any())).willReturn("{}");

            handler.onAuthenticationSuccess(request, response, auth);

            ArgumentCaptor<LoginResponse> bodyCaptor = ArgumentCaptor.forClass(LoginResponse.class);
            then(objectMapper).should().writeValueAsString(bodyCaptor.capture());

            assertThat(bodyCaptor.getValue().accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(bodyCaptor.getValue().refreshToken()).isNull();
        }

        @Test
        @DisplayName("deve definir status 200, contentType application/json e encoding UTF-8")
        void success_validOauth2Token_setsResponseMetadata() throws Exception {
            OAuth2AuthenticationToken auth = buildOauth2Token(VALID_EMAIL, VALID_NAME);
            HttpServletRequest request = buildRequest(DEVICE_INFO, null, IP_ADDRESS);
            HttpServletResponse response = buildWritableResponse();

            given(oauthLoginUseCase.execute(any(), any(), any(), any()))
                    .willReturn(new LoginResponse(ACCESS_TOKEN, REFRESH_TOKEN));
            given(objectMapper.writeValueAsString(any())).willReturn("{}");

            handler.onAuthenticationSuccess(request, response, auth);

            then(response).should().setStatus(HttpServletResponse.SC_OK);
            then(response).should().setContentType("application/json");
            then(response).should().setCharacterEncoding("UTF-8");
        }

        // --- ATRIBUTOS AUSENTES ------------------------------------------------------------------

        @Test
        @DisplayName("deve retornar 400 quando email está ausente nos atributos OAuth2")
        void success_missingEmail_sends400() throws Exception {
            OAuth2AuthenticationToken auth = buildOauth2Token(null, VALID_NAME);
            HttpServletRequest request = buildRequest(DEVICE_INFO, null, IP_ADDRESS);
            HttpServletResponse response = mock(HttpServletResponse.class);

            handler.onAuthenticationSuccess(request, response, auth);

            then(response).should().sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
            then(oauthLoginUseCase).should(never()).execute(any(), any(), any(), any());
        }

        @Test
        @DisplayName("deve retornar 400 quando name está ausente nos atributos OAuth2")
        void success_missingName_sends400() throws Exception {
            OAuth2AuthenticationToken auth = buildOauth2Token(VALID_EMAIL, null);
            HttpServletRequest request = buildRequest(DEVICE_INFO, null, IP_ADDRESS);
            HttpServletResponse response = mock(HttpServletResponse.class);

            handler.onAuthenticationSuccess(request, response, auth);

            then(response).should().sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
            then(oauthLoginUseCase).should(never()).execute(any(), any(), any(), any());
        }

        // --- AUTENTICAÇÃO NÃO OAUTH2 ------------------------------------------------------------------

        @Test
        @DisplayName("deve retornar 401 quando authentication não é OAuth2AuthenticationToken")
        void success_nonOauth2Authentication_sends401() throws Exception {
            Authentication auth = mock(Authentication.class);
            given(auth.getClass()).willCallRealMethod();
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);

            handler.onAuthenticationSuccess(request, response, auth);

            then(response).should().sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
            then(oauthLoginUseCase).should(never()).execute(any(), any(), any(), any());
        }

        @Test
        @DisplayName("deve completar sem exceção no fluxo feliz")
        void success_validOauth2Token_doesNotThrow() throws Exception {
            OAuth2AuthenticationToken auth = buildOauth2Token(VALID_EMAIL, VALID_NAME);
            HttpServletRequest request = buildRequest(DEVICE_INFO, null, IP_ADDRESS);
            HttpServletResponse response = buildWritableResponse();

            given(oauthLoginUseCase.execute(any(), any(), any(), any()))
                    .willReturn(new LoginResponse(ACCESS_TOKEN, REFRESH_TOKEN));
            given(objectMapper.writeValueAsString(any())).willReturn("{}");

            assertThatNoException()
                    .isThrownBy(() -> handler.onAuthenticationSuccess(request, response, auth));
        }
    }
}