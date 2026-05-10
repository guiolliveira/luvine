package com.javacore.spring_api_luvine.shared.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javacore.spring_api_luvine.auth.dto.LoginResponse;
import com.javacore.spring_api_luvine.auth.service.oauth.OauthService;
import com.javacore.spring_api_luvine.shared.util.EmailMask;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OauthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final OauthService oauthService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            OAuth2User oAuth2User = oauth2Token.getPrincipal();

            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String deviceInfo = request.getHeader("User-Agent");

            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }

            String maskedEmail = EmailMask.mask(email);
            log.info("event=oauth_callback_received provider={} email={} ip={}",
                    oauth2Token.getAuthorizedClientRegistrationId(), maskedEmail, ipAddress);

            if (email == null || name == null) {
                log.warn("event=oauth_callback_rejected reason=missing_attributes provider={} emailPresent={} namePresent={}",
                        oauth2Token.getAuthorizedClientRegistrationId(), email != null, name != null);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Falha na autenticação com o provedor externo");
                return;
            }

            LoginResponse loginResponse = oauthService.loginWithGoogle(email, name, deviceInfo, ipAddress);

            ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResponse.refreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .maxAge(7 * 24 * 60 * 60)
                    .path("/api/v1/auth/refresh")
                    .sameSite("None")
                    .build();

            response.setHeader("Set-Cookie", cookie.toString());
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            var write = response.getWriter();
            write.write(objectMapper.writeValueAsString(
                    new LoginResponse(loginResponse.accessToken(), null)
            ));
            write.flush();

            log.info("event=oauth_callback_completed provider={} email={} ip={}",
                    oauth2Token.getAuthorizedClientRegistrationId(), maskedEmail, ipAddress);
        } else {
            log.warn("event=oauth_callback_rejected reason=unsupported_authentication_type type={}",
                    authentication.getClass().getSimpleName());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Credenciais Inválidas");
        }
    }
}