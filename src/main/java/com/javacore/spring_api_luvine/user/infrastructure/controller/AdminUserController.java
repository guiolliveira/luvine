package com.javacore.spring_api_luvine.user.infrastructure.controller;

import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.dto.UpdateRoleRequest;
import com.javacore.spring_api_luvine.user.application.usecase.UpdateUserRoleUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@Tag(name = "admin — usuários", description = "Endpoints administrativos para gerenciamento de usuários")
public class AdminUserController {

    private final UpdateUserRoleUseCase updateRole;

    @Operation(
            summary = "Atualizar papel do usuário",
            description = "Altera o papel (role) de um usuário alvo. O actor deve " +
                    "ter autoridade superior à do alvo e não pode promover a si mesmo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Papel atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Papel já atribuído ao usuário"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão — autoridade insuficiente" +
                    " ou tentativa de auto-promoção"),
            @ApiResponse(responseCode = "404", description = "Usuário alvo não encontrado")
    })
    @PostMapping("/{publicId}/role")
    public ResponseEntity<Void> updateRole(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Parameter(description = "ID público do usuário alvo", required = true) @PathVariable UUID publicId,
            @RequestBody UpdateRoleRequest request) {
        updateRole.execute(currentUser, publicId, request);
        return ResponseEntity.noContent().build();
    }
}