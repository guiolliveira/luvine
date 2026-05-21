package com.javacore.spring_api_luvine.user.infrastructure.controller;

import com.javacore.spring_api_luvine.user.application.dto.AddressRequest;
import com.javacore.spring_api_luvine.user.application.dto.AddressResponse;
import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
@Tag(name = "endereços", description = "Endpoints para gerenciamento de endereços do usuário")
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "Cadastrar endereço", description = "Cria um novo endereço vinculado ao usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Endereço cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Endereço já cadastrado e ativo para este usuário")
    })
    @PostMapping
    public ResponseEntity<AddressResponse> create(
            @AuthenticationPrincipal CurrentUser user, @RequestBody @Valid AddressRequest request) {
        AddressResponse addressResponse = addressService.createAddress(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(addressResponse);
    }

    @Operation(summary = "Listar endereços", description = "Retorna todos os endereços ativos do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereços listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    @GetMapping
    public ResponseEntity<List<AddressResponse>> findAllAddresses(@AuthenticationPrincipal CurrentUser user) {
        List<AddressResponse> addressResponse = addressService.findAllAddresses(user);
        return ResponseEntity.ok(addressResponse);
    }

    @Operation(summary = "Definir endereço padrão", description =
            "Marca o endereço informado como padrão do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço padrão atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Endereço inativo"),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    @PatchMapping("/{publicId}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @AuthenticationPrincipal CurrentUser user, @PathVariable UUID publicId) {
        AddressResponse addressResponse = addressService.setDefaultAddress(user, publicId);
        return ResponseEntity.ok(addressResponse);
    }

    @Operation(summary = "Deletar endereço", description = "Desativa o endereço informado do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    @DeleteMapping("/{publicId}/delete")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CurrentUser user, @PathVariable UUID publicId) {
        addressService.deleteAddress(user, publicId);
        return ResponseEntity.noContent().build();
    }
}