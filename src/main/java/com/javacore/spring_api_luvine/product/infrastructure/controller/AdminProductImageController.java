package com.javacore.spring_api_luvine.product.infrastructure.controller;

import com.javacore.spring_api_luvine.product.application.dto.CreateImageRequest;
import com.javacore.spring_api_luvine.product.application.dto.ProductImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.ReorderImageRequest;
import com.javacore.spring_api_luvine.product.application.dto.UpdateImageAltTextRequest;
import com.javacore.spring_api_luvine.product.application.usecase.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/catalog/products")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AdminProductImageController {

    private final CreateProductImageUseCase createProductImageUseCase;
    private final UpdateProductImageAltTextUseCase updateProductImageAltTextUseCase;
    private final SetPrimaryProductImageUseCase setPrimaryProductImageUseCase;
    private final RemoveProductImageUseCase removeProductImageUseCase;
    private final ReorderProductImageUseCase reorderProductImageUseCase;

    @PostMapping(
            value = "/{productPublicId}/variants/{variantPublicId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductImageResponse> create(
            @PathVariable UUID productPublicId,
            @PathVariable UUID variantPublicId,
            @RequestPart("file") MultipartFile  file,
            @RequestParam("altText") String altText,
            @RequestParam("displayOrder") Integer displayOrder,
            @RequestParam(value = "primaryImage", defaultValue = "false") boolean primaryImage) {

        CreateImageRequest request = new CreateImageRequest(altText, displayOrder, primaryImage);

        ProductImageResponse response =
                createProductImageUseCase.execute(productPublicId, variantPublicId, file, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{productPublicId}/variants/{variantPublicId}/images/{imagePublicId}")
    public ResponseEntity<ProductImageResponse> update(
            @PathVariable UUID productPublicId,
            @PathVariable UUID variantPublicId,
            @PathVariable UUID imagePublicId,
            @RequestBody @Valid UpdateImageAltTextRequest request) {

        ProductImageResponse response =
                updateProductImageAltTextUseCase.execute(productPublicId, variantPublicId, imagePublicId, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productPublicId}/variants/{variantPublicId}/images/{imagePublicId}/primary")
    public ResponseEntity<ProductImageResponse> setPrimaryImage(
            @PathVariable UUID productPublicId,
            @PathVariable UUID variantPublicId,
            @PathVariable UUID imagePublicId) {

        ProductImageResponse response =
                setPrimaryProductImageUseCase.execute(productPublicId, variantPublicId, imagePublicId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productPublicId}/variants/{variantPublicId}/images/{imagePublicId}")
    public ResponseEntity<Void> remove(
            @PathVariable UUID productPublicId,
            @PathVariable UUID variantPublicId,
            @PathVariable UUID imagePublicId) {

        removeProductImageUseCase.execute(productPublicId, variantPublicId, imagePublicId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productPublicId}/variants/{variantPublicId}/images/reoder")
    public ResponseEntity<List<ProductImageResponse>> reorder(
            @PathVariable UUID productPublicId,
            @PathVariable UUID variantPublicId,
            @RequestBody @Valid ReorderImageRequest request) {

        List<ProductImageResponse> response =
                reorderProductImageUseCase.execute(productPublicId, variantPublicId, request);

        return ResponseEntity.ok(response);
    }
}