package com.javacore.spring_api_luvine.auth.controller;

import com.javacore.spring_api_luvine.auth.dto.*;
import com.javacore.spring_api_luvine.auth.service.AuthService;
import com.javacore.spring_api_luvine.shared.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "autenticação", description = "Endpoints de autenticação e gerenciamento de sessão")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar usuário", description = "Cria uma nova conta e envia email de verificação")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Login", description = "Autentica o usuário e retorna access token + refresh token via cookie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou email não verificado"),
            @ApiResponse(responseCode = "403", description = "Conta desabilitada")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {

        String deviceInfo = httpRequest.getHeader("User-Agent");

        String ipAddress = httpRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        LoginResponse loginResponse = authService.login(request, deviceInfo, ipAddress);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .maxAge(7 * 24 * 60 * 60)
                .path("/api/v1/auth/refresh")
                .sameSite("Strict")
                .build();


        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(new LoginResponse(loginResponse.accessToken(), null));
    }

    @Operation(summary = "Renovar token", description = "Gera novos access e refresh tokens a partir do cookie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@CookieValue("refreshToken") String refreshToken) {

        LoginResponse loginResponse = authService.refresh(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .maxAge(7 * 24 * 60 * 60)
                .path("/api/v1/auth/refresh")
                .sameSite("Strict")
                .build();


        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(new LoginResponse(loginResponse.accessToken(), null));
    }

    @Operation(summary = "Verificar email", description = "Valida o código de verificação enviado por email")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email verificado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Código inválido ou expirado"),
            @ApiResponse(responseCode = "409", description = "Email já verificado")
    })
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestBody @Valid VerifyEmailRequest request) {
        MessageResponse response = authService.verifyEmail(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reenviar email de verificação", description = "Reenvia o código para o email informado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email reenviado com sucesso"),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas, tente mais tarde")
    })
    @PostMapping("/resend-email")
    public ResponseEntity<MessageResponse> resendEmail(@RequestBody @Valid ResendEmailRequest request) {
        MessageResponse response = authService.resendEmail(request);
        return ResponseEntity.ok(response);
    }
}