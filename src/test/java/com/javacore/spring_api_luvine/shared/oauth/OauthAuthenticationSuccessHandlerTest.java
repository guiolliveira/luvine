package com.javacore.spring_api_luvine.shared.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javacore.spring_api_luvine.auth.dto.LoginResponse;
import com.javacore.spring_api_luvine.auth.service.oauth.OauthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("OauthAuthenticationSuccessHandler")
@ExtendWith(MockitoExtension.class)
class OauthAuthenticationSuccessHandlerTest {

    @Mock private OauthService oauthService;
    @Mock private ObjectMapper objectMapper;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private OAuth2AuthenticationToken oauth2Token;
    @Mock private OAuth2User oAuth2User;

    @InjectMocks
    private OauthAuthenticationSuccessHandler handler;

    PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        printWriter = new PrintWriter(new StringWriter());
    }

    // --- HELPERS --------------------------------------------------------------

    private void setupOAuth2Token() {
        when(oauth2Token.getPrincipal()).thenReturn(oAuth2User);
        when(oauth2Token.getAuthorizedClientRegistrationId()).thenReturn("google");
    }

    private void setupValidAttributes() {
        when(oAuth2User.getAttribute("email")).thenReturn("user@example.com");
        when(oAuth2User.getAttribute("name")).thenReturn("John Doe");
    }

    private void setupHeaders(String userAgent, String xForwardedFor) {
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
        when(request.getHeader("X-Forwarded-For")).thenReturn(xForwardedFor);
    }

    private void setupLoginResponse() {
        when(oauthService.loginWithGoogle(anyString(), anyString(), any(), any()))
                .thenReturn(new LoginResponse("access-token-123", "refresh-token-456"));
    }

    // --- FLUXO FELIZ ----------------------------------------------------------

    @Nested
    @DisplayName("onAuthenticationSuccess() — fluxo feliz")
    class HappyPath {

        @BeforeEach
        void setUp() throws Exception {
            when(response.getWriter()).thenReturn(printWriter);
            setupOAuth2Token();
            setupValidAttributes();
            setupLoginResponse();
            setupHeaders("Mozilla/5.0", "192.168.0.1");
            when(objectMapper.writeValueAsString(any()))
                    .thenReturn("{\"accessToken\":\"access-token-123\",\"refreshToken\":null}");
        }

        @Test
        @DisplayName("deve chamar oauthService.loginWithGoogle com email e nome corretos")
        void shouldCallOauthServiceWithCorrectEmailAndName() throws Exception {
            handler.onAuthenticationSuccess(request, response, oauth2Token);

            verify(oauthService).loginWithGoogle(
                    eq("user@example.com"), eq("John Doe"), any(), any());
        }

        @Test
        @DisplayName("deve passar o User-Agent como deviceInfo para o service")
        void shouldPassUserAgentAsDeviceInfo() throws Exception {
            handler.onAuthenticationSuccess(request, response, oauth2Token);

            verify(oauthService).loginWithGoogle(any(), any(), eq("Mozilla/5.0"), any());
        }

        @Test
        @DisplayName("deve usar X-Forwarded-For como ipAddress quando presente")
        void shouldUseXForwardedForAsIpWhenPresent() throws Exception {
            handler.onAuthenticationSuccess(request, response, oauth2Token);

            verify(oauthService).loginWithGoogle(any(), any(), any(), eq("192.168.0.1"));
        }

        @Test
        @DisplayName("deve usar remoteAddr como ipAddress quando X-Forwarded-For estiver ausente")
        void shouldFallbackToRemoteAddrWhenXForwardedForIsNull() throws Exception {
            // Sobrescreve apenas o X-Forwarded-For para null neste teste específico
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("10.0.0.1");

            handler.onAuthenticationSuccess(request, response, oauth2Token);

            verify(oauthService).loginWithGoogle(any(), any(), any(), eq("10.0.0.1"));
        }

        @Test
        @DisplayName("deve setar status 200 na resposta")
        void shouldSetStatus200() throws Exception {
            handler.onAuthenticationSuccess(request, response, oauth2Token);

            verify(response).setStatus(HttpServletResponse.SC_OK);
        }

        @Test
        @DisplayName("deve setar Content-Type como application/json")
        void shouldSetContentTypeJson() throws Exception {
            handler.onAuthenticationSuccess(request, response, oauth2Token);

            verify(response).setContentType("application/json");
        }

        @Test
        @DisplayName("deve setar encoding UTF-8 na resposta")
        void shouldSetCharacterEncodingUtf8() throws Exception {
            handler.onAuthenticationSuccess(request, response, oauth2Token);

            verify(response).setCharacterEncoding("UTF-8");
        }

        @Test
        @DisplayName("deve escrever o accessToken no body e omitir o refreshToken")
        void shouldWriteAccessTokenAndOmitRefreshToken() throws Exception {
            handler.onAuthenticationSuccess(request, response, oauth2Token);

            verify(objectMapper).writeValueAsString(
                    argThat(arg -> arg instanceof LoginResponse response
                            && "access-token-123".equals(response.accessToken())
                            && response.refreshToken() == null)
            );
        }

        @Test
        @DisplayName("deve setar o cookie refreshToken como HttpOnly e Secure")
        void shouldSetRefreshTokenCookieHttpOnlyAndSecure() throws Exception {
            handler.onAuthenticationSuccess(request, response, oauth2Token);

            verify(response).setHeader(eq("Set-Cookie"), argThat(cookie ->
                    cookie.contains("refreshToken=refresh-token-456")
                            && cookie.contains("HttpOnly")
                            && cookie.contains("Secure")));
        }

        @Test
        @DisplayName("deve setar o cookie com path /auth/refresh")
        void shouldSetRefreshTokenCookieWithCorrectPath() throws Exception {
            handler.onAuthenticationSuccess(request, response, oauth2Token);

            verify(response).setHeader(eq("Set-Cookie"),
                    argThat(cookie -> cookie.contains("Path=/api/v1/auth/refresh")));
        }

        @Test
        @DisplayName("deve setar o cookie com SameSite=None")
        void shouldSetRefreshTokenCookieWithSameSiteNone() throws Exception {
            handler.onAuthenticationSuccess(request, response, oauth2Token);

            verify(response).setHeader(eq("Set-Cookie"),
                    argThat(cookie -> cookie.contains("SameSite=None")));
        }
    }

    // --- ATRIBUTOS AUSENTES ---------------------------------------------------

    @Nested
    @DisplayName("onAuthenticationSuccess() — atributos OAuth ausentes")
    class MissingAttributes {

        @BeforeEach
        void setUp() {
            setupOAuth2Token();
            setupHeaders(null, null);
        }

        @Test
        @DisplayName("deve retornar 400 quando email for nulo")
        void shouldReturn400WhenEmailIsNull() throws Exception {
            when(oAuth2User.getAttribute("email")).thenReturn(null);
            when(oAuth2User.getAttribute("name")).thenReturn("John Doe");

            handler.onAuthenticationSuccess(request, response, oauth2Token);

            verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
        }

        @Test
        @DisplayName("deve retornar 400 quando name for nulo")
        void shouldReturn400WhenNameIsNull() throws Exception {
            when(oAuth2User.getAttribute("email")).thenReturn("user@example.com");
            when(oAuth2User.getAttribute("name")).thenReturn(null);

            handler.onAuthenticationSuccess(request, response, oauth2Token);

            verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
        }

        @Test
        @DisplayName("não deve chamar oauthService quando atributos estiverem ausentes")
        void shouldNotCallOauthServiceWhenAttributesAreMissing() throws Exception {
            when(oAuth2User.getAttribute("email")).thenReturn(null);
            when(oAuth2User.getAttribute("name")).thenReturn(null);

            handler.onAuthenticationSuccess(request, response, oauth2Token);

            verifyNoInteractions(oauthService);
        }
    }

    // --- TIPO DE AUTENTICAÇÃO INESPERADO --------------------------------------

    @Nested
    @DisplayName("onAuthenticationSuccess() — tipo de autenticação inesperado")
    class UnexpectedAuthType {

        @Test
        @DisplayName("deve retornar 401 quando authentication não for OAuth2AuthenticationToken")
        void shouldReturn401WhenAuthIsNotOAuth2Token() throws Exception {
            Authentication otherAuth = mock(Authentication.class);

            handler.onAuthenticationSuccess(request, response, otherAuth);

            verify(response).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
        }

        @Test
        @DisplayName("não deve chamar oauthService quando tipo de autenticação for inesperado")
        void shouldNotCallOauthServiceWhenAuthTypeIsUnexpected() throws Exception {
            Authentication otherAuth = mock(Authentication.class);

            handler.onAuthenticationSuccess(request, response, otherAuth);

            verifyNoInteractions(oauthService);
        }
    }
}