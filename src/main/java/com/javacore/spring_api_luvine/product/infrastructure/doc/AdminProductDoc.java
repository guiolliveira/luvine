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

@Tag(name = "Admin — Products", description = "Gerenciamento de produtos do catálogo" +
        " (acesso restrito a administradores)")
@SecurityRequirement(name = "bearerAuth")
public interface AdminProductDoc {

    @Operation(
            summary = "Criar produto",
            description = "Cria um novo produto no catálogo. É obrigatório informar ao" +
                    " menos uma variante e uma categoria ativa."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProductDetailsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nenhuma variante informada",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Já existe um produto com esse nome", content = @Content)
    })
    @PostMapping
    ResponseEntity<ProductDetailsResponse> create(@RequestBody @Valid CreateProductRequest request);


    @Operation(
            summary = "Atualizar produto",
            description = "Atualiza os dados de um produto existente. Apenas os campos informados serão alterados. " +
                    "A categoria informada deve estar ativa."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProductDetailsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto ou categoria não encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Já existe um produto com esse nome", content = @Content)
    })
    @PatchMapping("/{productPublicId}")
    ResponseEntity<ProductDetailsResponse> update(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId,
            @RequestBody @Valid UpdateProductRequest request);


    @Operation(
            summary = "Buscar detalhes do produto",
            description = "Retorna os detalhes completos de um produto, incluindo todas as suas variantes e imagens."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    @GetMapping("/{productPublicId}")
    ResponseEntity<ProductDetailsResponse> getProductsDetails(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId);


    @Operation(
            summary = "Listar e pesquisar produtos",
            description = "Retorna uma lista paginada de produtos com suporte a " +
                    "filtros por nome, categoria, status, faixa de preço e outros critérios."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Parâmetros de busca inválidos", content = @Content)
    })
    @GetMapping
    ResponseEntity<Page<ProductSummaryResponse>> searchProduct(
            @ParameterObject @Valid SearchProductRequest request,
            @ParameterObject Pageable pageable);
}