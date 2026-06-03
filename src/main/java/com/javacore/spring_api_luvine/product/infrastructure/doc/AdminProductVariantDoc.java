package com.javacore.spring_api_luvine.product.infrastructure.doc;

import com.javacore.spring_api_luvine.product.application.dto.CreateVariantRequest;
import com.javacore.spring_api_luvine.product.application.dto.ProductVariantResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateVariantRequest;
import com.javacore.spring_api_luvine.product.application.dto.UpdateVariantStockRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin — Product Variants", description = "Gerenciamento de variantes de produtos" +
        " (acesso restrito a administradores)")
@SecurityRequirement(name = "bearerAuth")
public interface AdminProductVariantDoc {

    @Operation(
            summary = "Criar variante",
            description = "Adiciona uma nova variante ao produto. Cada combinação de cor e tamanho" +
                    " deve ser única dentro do produto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Variante criada com sucesso",
                    content = @Content(schema = @Schema(implementation = ProductVariantResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Já existe uma variante com essa combinação" +
                    " de cor e tamanho", content = @Content)
    })
    @PostMapping("/{productPublicId}/variants")
    ResponseEntity<ProductVariantResponse> create(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId,
            @RequestBody @Valid CreateVariantRequest request);


    @Operation(
            summary = "Atualizar variante",
            description = "Atualiza os atributos de uma variante existente, como cor, tamanho ou preço. " +
                    "Apenas os campos informados serão alterados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Variante atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = ProductVariantResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto ou variante não encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Já existe uma variante com essa" +
                    " combinação de cor e tamanho", content = @Content)
    })
    @PatchMapping("/{productPublicId}/variants/{variantPublicId}")
    ResponseEntity<ProductVariantResponse> update(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId,
            @Parameter(description = "Identificador público da variante", required = true)
            @PathVariable UUID variantPublicId,
            @RequestBody @Valid UpdateVariantRequest request);


    @Operation(
            summary = "Ativar variante",
            description = "Ativa uma variante previamente desativada, tornando-a disponível para compra pelos clientes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Variante ativada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto ou variante não encontrada", content = @Content)
    })
    @PatchMapping("/{productPublicId}/variants/{variantPublicId}/activate")
    ResponseEntity<Void> activate(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId,
            @Parameter(description = "Identificador público da variante", required = true)
            @PathVariable UUID variantPublicId);


    @Operation(
            summary = "Desativar variante",
            description = "Desativa uma variante, ocultando-a dos clientes sem removê-la permanentemente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Variante desativada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto ou variante não encontrada", content = @Content)
    })
    @PatchMapping("/{productPublicId}/variants/{variantPublicId}/deactivate")
    ResponseEntity<Void> deactivate(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId,
            @Parameter(description = "Identificador público da variante", required = true)
            @PathVariable UUID variantPublicId);


    @Operation(
            summary = "Aumentar estoque da variante",
            description = "Incrementa a quantidade em estoque de uma variante pelo valor informado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estoque incrementado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProductVariantResponse.class))),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto ou variante não encontrada", content = @Content)
    })
    @PatchMapping("/{productPublicId}/variants/{variantPublicId}/stock/increase")
    ResponseEntity<ProductVariantResponse> increase(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId,
            @Parameter(description = "Identificador público da variante", required = true)
            @PathVariable UUID variantPublicId,
            @RequestBody @Valid UpdateVariantStockRequest request);


    @Operation(
            summary = "Diminuir estoque da variante",
            description = "Decrementa a quantidade em estoque de uma variante pelo valor informado. " +
                    "Não é permitido reduzir o estoque abaixo de zero."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estoque decrementado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProductVariantResponse.class))),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida ou estoque insuficiente",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto ou variante não encontrada", content = @Content)
    })
    @PatchMapping("/{productPublicId}/variants/{variantPublicId}/stock/decrease")
    ResponseEntity<ProductVariantResponse> decrease(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId,
            @Parameter(description = "Identificador público da variante", required = true)
            @PathVariable UUID variantPublicId,
            @RequestBody @Valid UpdateVariantStockRequest request);


    @Operation(
            summary = "Remover variante",
            description = "Remove permanentemente uma variante do produto, incluindo todas as suas imagens associadas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Variante removida com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto ou variante não encontrada", content = @Content)
    })
    @DeleteMapping("/{productPublicId}/variants/{variantPublicId}")
    ResponseEntity<Void> delete(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId,
            @Parameter(description = "Identificador público da variante", required = true)
            @PathVariable UUID variantPublicId);
}