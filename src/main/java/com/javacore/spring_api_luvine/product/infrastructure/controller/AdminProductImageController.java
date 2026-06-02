package com.javacore.spring_api_luvine.product.infrastructure.controller;

import com.javacore.spring_api_luvine.product.application.dto.CreateImageRequest;
import com.javacore.spring_api_luvine.product.application.dto.ProductImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.ReorderImageRequest;
import com.javacore.spring_api_luvine.product.application.dto.UpdateImageAltTextRequest;
import com.javacore.spring_api_luvine.product.application.usecase.*;
import com.javacore.spring_api_luvine.product.infrastructure.doc.AdminProductImageDoc;
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
public class AdminProductImageController implements AdminProductImageDoc {

    private final CreateProductImageUseCase createProductImageUseCase;
    private final UpdateProductImageAltTextUseCase updateProductImageAltTextUseCase;
    private final SetPrimaryProductImageUseCase setPrimaryProductImageUseCase;
    private final RemoveProductImageUseCase removeProductImageUseCase;
    private final ReorderProductImageUseCase reorderProductImageUseCase;

    @Override
    public ResponseEntity<ProductImageResponse> create(
            UUID productPublicId, UUID variantPublicId,
            MultipartFile file, String altText, Integer displayOrder, boolean primaryImage) {
        CreateImageRequest request = new CreateImageRequest(altText, displayOrder, primaryImage);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createProductImageUseCase.execute(productPublicId, variantPublicId, file, request));
    }

    @Override
    public ResponseEntity<ProductImageResponse> update(
            UUID productPublicId, UUID variantPublicId,
            UUID imagePublicId, UpdateImageAltTextRequest request) {
        return ResponseEntity.ok(
                updateProductImageAltTextUseCase.execute(productPublicId, variantPublicId, imagePublicId, request));
    }

    @Override
    public ResponseEntity<ProductImageResponse> setPrimaryImage(
            UUID productPublicId, UUID variantPublicId, UUID imagePublicId) {
        return ResponseEntity.ok(
                setPrimaryProductImageUseCase.execute(productPublicId, variantPublicId, imagePublicId));
    }

    @Override
    public ResponseEntity<Void> remove(
            UUID productPublicId, UUID variantPublicId, UUID imagePublicId) {
        removeProductImageUseCase.execute(productPublicId, variantPublicId, imagePublicId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ProductImageResponse>> reorder(
            UUID productPublicId, UUID variantPublicId, ReorderImageRequest request) {
        return ResponseEntity.ok(
                reorderProductImageUseCase.execute(productPublicId, variantPublicId, request));
    }
}