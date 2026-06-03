package com.javacore.spring_api_luvine.product.infrastructure.doc;

import com.javacore.spring_api_luvine.product.application.dto.ProductImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.ReorderImageRequest;
import com.javacore.spring_api_luvine.product.application.dto.UpdateImageAltTextRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Admin — Product Images", description = "Gerenciamento de imagens de" +
        " variantes de produtos (acesso restrito a administradores)")
@SecurityRequirement(name = "bearerAuth")
public interface AdminProductImageDoc {

    @Operation(
            summary = "Enviar imagem da variante",
            description = "Realiza o upload de uma imagem para uma variante específica do produto. " +
                    "Formatos aceitos: JPEG, PNG e WebP. Tamanho máximo: 5MB. " +
                    "Caso `primaryImage` seja `true`, esta imagem será definida como principal da variante."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Imagem enviada com sucesso",
                    content = @Content(schema = @Schema(implementation = ProductImageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido, ausente ou parâmetros malformados",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto ou variante não encontrada", content = @Content)
    })
    @PostMapping(value = "/{productPublicId}/variants/{variantPublicId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ProductImageResponse> create(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId,
            @Parameter(description = "Identificador público da variante", required = true)
            @PathVariable UUID variantPublicId,
            @Parameter(description = "Arquivo de imagem (JPEG, PNG ou WebP, máx. 5MB)", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Texto alternativo da imagem para acessibilidade e SEO", required = true)
            @RequestParam("altText") String altText,
            @Parameter(description = "Posição de exibição da imagem na galeria", required = true)
            @RequestParam("displayOrder") Integer displayOrder,
            @Parameter(description = "Define se esta imagem será a principal da variante. Padrão: false")
            @RequestParam(value = "primaryImage", defaultValue = "false") boolean primaryImage);


    @Operation(
            summary = "Atualizar texto alternativo da imagem",
            description = "Atualiza o texto alternativo (alt text) de uma imagem de variante, utilizado" +
                    " para acessibilidade e SEO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alt text atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProductImageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto, variante ou imagem não encontrada",
                    content = @Content)
    })
    @PatchMapping("/{productPublicId}/variants/{variantPublicId}/images/{imagePublicId}")
    ResponseEntity<ProductImageResponse> update(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId,
            @Parameter(description = "Identificador público da variante", required = true)
            @PathVariable UUID variantPublicId,
            @Parameter(description = "Identificador público da imagem", required = true)
            @PathVariable UUID imagePublicId,
            @RequestBody @Valid UpdateImageAltTextRequest request);


    @Operation(
            summary = "Definir imagem principal da variante",
            description = "Define uma imagem específica como principal da variante. " +
                    "A imagem principal é exibida como destaque nos detalhes e listagens do produto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagem principal definida com sucesso",
                    content = @Content(schema = @Schema(implementation = ProductImageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto, variante ou imagem não encontrada",
                    content = @Content)
    })
    @PatchMapping("/{productPublicId}/variants/{variantPublicId}/images/{imagePublicId}/primary")
    ResponseEntity<ProductImageResponse> setPrimaryImage(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId,
            @Parameter(description = "Identificador público da variante", required = true)
            @PathVariable UUID variantPublicId,
            @Parameter(description = "Identificador público da imagem", required = true)
            @PathVariable UUID imagePublicId);


    @Operation(
            summary = "Remover imagem da variante",
            description = "Remove permanentemente uma imagem da variante do produto, incluindo o arquivo no storage."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Imagem removida com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto, variante ou imagem não encontrada",
                    content = @Content)
    })
    @DeleteMapping("/{productPublicId}/variants/{variantPublicId}/images/{imagePublicId}")
    ResponseEntity<Void> remove(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId,
            @Parameter(description = "Identificador público da variante", required = true)
            @PathVariable UUID variantPublicId,
            @Parameter(description = "Identificador público da imagem", required = true)
            @PathVariable UUID imagePublicId);


    @Operation(
            summary = "Reordenar imagens da variante",
            description = "Reordena as imagens de uma variante conforme a lista de identificadores informada. " +
                    "A ordem dos IDs na lista define a nova ordem de exibição."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagens reordenadas com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou IDs não correspondem" +
                    " às imagens da variante", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto ou variante não encontrada", content = @Content)
    })
    @PatchMapping("/{productPublicId}/variants/{variantPublicId}/images/reorder")
    ResponseEntity<List<ProductImageResponse>> reorder(
            @Parameter(description = "Identificador público do produto", required = true)
            @PathVariable UUID productPublicId,
            @Parameter(description = "Identificador público da variante", required = true)
            @PathVariable UUID variantPublicId,
            @RequestBody @Valid ReorderImageRequest request);
}