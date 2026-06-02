package com.javacore.spring_api_luvine.product.infrastructure.doc;

import com.javacore.spring_api_luvine.product.application.dto.ProductDetailsResponse;
import com.javacore.spring_api_luvine.product.application.dto.ProductSummaryResponse;
import com.javacore.spring_api_luvine.product.application.dto.SearchProductRequest;
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

@Tag(name = "Products", description = "Consulta pública de produtos disponíveis no catálogo")
@SecurityRequirement(name = "bearerAuth")
public interface PublicProductDoc {

    @Operation(
            summary = "Buscar detalhes do produto",
            description = "Retorna os detalhes completos de um produto ativo, incluindo todas as variantes e imagens. " +
                    "O endpoint utiliza slug + ID para SEO. Caso o slug esteja desatualizado (produto renomeado), " +
                    "um redirecionamento permanente (308) é retornado automaticamente para a URL canônica atualizada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductDetailsResponse.class))),
            @ApiResponse(responseCode = "308", description = "Slug desatualizado — redirecionamento" +
                    " permanente para a URL canônica",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado ou inativo", content = @Content)
    })
    @GetMapping("/{slug}-{productPublicId}")
    ResponseEntity<?> getProductDetails(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId,
            @Parameter(description = "Slug atual do produto, utilizado para SEO e URL canônica", required = true)
            @PathVariable String slug);


    @Operation(
            summary = "Listar e pesquisar produtos",
            description = "Retorna uma lista paginada de produtos ativos com suporte a filtros por nome, categoria, " +
                    "faixa de preço e outros critérios. Produtos inativos são automaticamente excluídos dos resultados."
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