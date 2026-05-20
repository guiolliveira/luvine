package com.javacore.spring_api_luvine.user.infrastructure.controller;

import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.dto.ProfileResponse;
import com.javacore.spring_api_luvine.user.application.dto.UpdateProfileRequest;
import com.javacore.spring_api_luvine.user.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
@Tag(name = "usuário", description = "Endpoints de gerenciamento do perfil do usuário autenticado")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Buscar perfil", description = "Retorna os dados do perfil do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso")
    })
    @GetMapping
    public ResponseEntity<ProfileResponse> profile(@AuthenticationPrincipal CurrentUser currentUser) {
        ProfileResponse profileResponse = userService.findProfile(currentUser);
        return ResponseEntity.ok(profileResponse);
    }

    @Operation(summary = "Atualizar perfil", description = "Atualiza o primeiro e/ou último nome do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Perfil atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou valor igual ao atual"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso")
    })
    @PostMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestBody @Valid UpdateProfileRequest request) {
        userService.updateProfile(currentUser, request);
        return ResponseEntity.noContent().build();
    }
}