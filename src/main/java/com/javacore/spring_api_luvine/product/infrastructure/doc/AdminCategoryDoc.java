package com.javacore.spring_api_luvine.product.infrastructure.doc;

import com.javacore.spring_api_luvine.product.application.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin — Categories", description = "Gerenciamento de categorias (acesso restrito a administradores)")
@SecurityRequirement(name = "bearerAuth")
public interface AdminCategoryDoc {

    @Operation(
            summary = "Criar categoria",
            description = "Cria uma nova categoria. Caso `parentPublicId` " +
                    "seja informado, a categoria será criada como subcategoria."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryDetailsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Já existe uma categoria com esse nome",
                    content = @Content)
    })
    @PostMapping
    ResponseEntity<CategoryDetailsResponse> create(@RequestBody @Valid CreateCategoryRequest request);


    @Operation(
            summary = "Atualizar categoria",
            description = "Atualiza nome, descrição ou categoria pai. Apenas os campos informados serão alterados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryDetailsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Já existe uma categoria com esse nome", content = @Content)
    })
    @PatchMapping("/{categoryPublicId}")
    ResponseEntity<CategoryDetailsResponse> update(
            @Parameter(description = "Identificador público da categoria", required = true)
            @PathVariable UUID categoryPublicId,
            @RequestBody @Valid UpdateCategoryRequest request);


    @Operation(
            summary = "Ativar categoria",
            description = "Ativa uma categoria previamente desativada, tornando-a visível para os clientes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria ativada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content)
    })
    @PatchMapping("/{categoryPublicId}/activate")
    ResponseEntity<Void> activate(
            @Parameter(description = "Identificador público da categoria", required = true)
            @PathVariable UUID categoryPublicId);


    @Operation(
            summary = "Desativar categoria",
            description = "Desativa uma categoria, ocultando-a dos clientes sem removê-la permanentemente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria desativada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content)
    })
    @PatchMapping("/{categoryPublicId}/deactivate")
    ResponseEntity<Void> deactivate(
            @Parameter(description = "Identificador público da categoria", required = true)
            @PathVariable UUID categoryPublicId);


    @Operation(
            summary = "Buscar detalhes da categoria",
            description = "Retorna os detalhes completos de uma categoria, incluindo subcategorias e imagem."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada",
                    content = @Content(schema = @Schema(implementation = CategoryDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content)
    })
    @GetMapping("/{categoryPublicId}")
    ResponseEntity<CategoryDetailsResponse> getCategoryDetails(
            @Parameter(description = "Identificador público da categoria", required = true)
            @PathVariable UUID categoryPublicId);


    @Operation(
            summary = "Listar e pesquisar categorias",
            description = "Retorna uma lista paginada de categorias com suporte a filtros " +
                    "por nome, slug do pai, status e tipo (raiz ou subcategoria)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Parâmetros de busca inválidos", content = @Content)
    })
    @GetMapping
    ResponseEntity<Page<CategorySummaryResponse>> searchProduct(
            @ParameterObject @Valid SearchCategoryRequest request,
            @ParameterObject Pageable pageable);
}