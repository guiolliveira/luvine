package com.javacore.spring_api_luvine.product.infrastructure.doc;

import com.javacore.spring_api_luvine.product.application.dto.CategoryDetailsResponse;
import com.javacore.spring_api_luvine.product.application.dto.CategorySummaryResponse;
import com.javacore.spring_api_luvine.product.application.dto.SearchCategoryRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Tag(name = "Categories", description = "Consulta pública de categorias disponíveis no catálogo")
@SecurityRequirement(name = "bearerAuth")
public interface PublicCategoryDoc {

    @Operation(
            summary = "Buscar detalhes da categoria",
            description = "Retorna os detalhes completos de uma categoria ativa, incluindo subcategorias e imagem. " +
                    "Categorias inativas não são retornadas neste endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada",
                    content = @Content(schema = @Schema(implementation = CategoryDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou inativa", content = @Content)
    })
    @GetMapping("/{categoryPublicId}")
    ResponseEntity<CategoryDetailsResponse> getCategoryDetails(
            @Parameter(description = "Identificador público da categoria", required = true)
            @PathVariable UUID categoryPublicId);


    @Operation(
            summary = "Listar e pesquisar categorias",
            description = "Retorna uma lista paginada de categorias ativas com suporte" +
                    " a filtros por nome, slug do pai e tipo (raiz ou subcategoria). " +
                    "Categorias inativas são automaticamente excluídas dos resultados."
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