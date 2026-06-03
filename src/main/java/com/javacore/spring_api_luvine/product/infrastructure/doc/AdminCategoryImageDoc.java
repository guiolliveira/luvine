package com.javacore.spring_api_luvine.product.infrastructure.doc;

import com.javacore.spring_api_luvine.product.application.dto.CategoryImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.CreateCategoryImageRequest;
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

import java.util.UUID;

@Tag(name = "Admin — Category Images", description = "Gerenciamento de imagens de categorias " +
        "(acesso restrito a administradores)")
@SecurityRequirement(name = "bearerAuth")
public interface AdminCategoryImageDoc {

    @Operation(
            summary = "Enviar imagem da categoria",
            description = "Realiza o upload de uma imagem para a categoria informada. Caso já exista" +
                    " uma imagem, ela será substituída. " +
                    "Formatos aceitos: JPEG, PNG e WebP. Tamanho máximo: 5MB."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Imagem enviada com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryImageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido, ausente ou dados malformados",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content)
    })
    @PostMapping(value = "/{categoryPublicId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<CategoryImageResponse> create(
            @Parameter(description = "Identificador público da categoria", required = true)
            @PathVariable UUID categoryPublicId,
            @Parameter(description = "Arquivo de imagem (JPEG, PNG ou WebP, máx. 5MB)", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Texto alternativo da imagem para acessibilidade e SEO", required = true)
            @RequestParam("altText") String altText);


    @Operation(
            summary = "Atualizar texto alternativo da imagem",
            description = "Atualiza o texto alternativo (alt text) de uma" +
                    " imagem de categoria, utilizado para acessibilidade e SEO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alt text atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryImageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria ou imagem não encontrada", content = @Content)
    })
    @PatchMapping("/{categoryPublicId}/image/{imagePublicId}")
    ResponseEntity<CategoryImageResponse> update(
            @Parameter(description = "Identificador público da categoria", required = true)
            @PathVariable UUID categoryPublicId,
            @Parameter(description = "Identificador público da imagem", required = true)
            @PathVariable UUID imagePublicId,
            @RequestBody @Valid UpdateImageAltTextRequest request);

    @Operation(
            summary = "Remover imagem da categoria",
            description = "Remove a imagem associada à categoria e exclui o arquivo do storage."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Imagem removida com sucesso",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria ou imagem não encontrada",
                    content = @Content)
    })
    @DeleteMapping("/{categoryPublicId}/image/{imagePublicId}")
    ResponseEntity<Void> remove(
            @Parameter(description = "Identificador público da categoria", required = true)
            @PathVariable UUID categoryPublicId,
            @Parameter(description = "Identificador público da imagem", required = true)
            @PathVariable UUID imagePublicId);
}