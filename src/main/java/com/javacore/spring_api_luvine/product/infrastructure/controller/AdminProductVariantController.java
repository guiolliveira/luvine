package com.javacore.spring_api_luvine.product.infrastructure.controller;

import com.javacore.spring_api_luvine.product.application.dto.CreateVariantRequest;
import com.javacore.spring_api_luvine.product.application.dto.ProductVariantResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateVariantRequest;
import com.javacore.spring_api_luvine.product.application.dto.UpdateVariantStockRequest;
import com.javacore.spring_api_luvine.product.application.usecase.*;
import com.javacore.spring_api_luvine.product.infrastructure.doc.AdminProductVariantDoc;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/catalog/products")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AdminProductVariantController implements AdminProductVariantDoc {

    private final CreateProductVariantUseCase createProductVariantUseCase;
    private final UpdateProductVariantUseCase updateProductVariantUseCase;
    private final ActivateProductVariantUseCase activateProductVariantUseCase;
    private final DeactivateProductVariantUseCase deactivateProductVariantUseCase;
    private final IncreaseProductVariantStockUseCase increaseProductVariantStockUseCase;
    private final DecreaseProductVariantUseCase decreaseProductVariantUseCase;
    private final RemoveProductVariantUseCase removeProductVariantUseCase;

    @Override
    public ResponseEntity<ProductVariantResponse> create(UUID productPublicId, CreateVariantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createProductVariantUseCase.execute(productPublicId, request));
    }

    @Override
    public ResponseEntity<ProductVariantResponse> update(
            UUID productPublicId, UUID variantPublicId, UpdateVariantRequest request) {
        return ResponseEntity.ok(
                updateProductVariantUseCase.execute(productPublicId, variantPublicId, request));
    }

    @Override
    public ResponseEntity<Void> activate(UUID productPublicId, UUID variantPublicId) {
        activateProductVariantUseCase.execute(productPublicId, variantPublicId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deactivate(UUID productPublicId, UUID variantPublicId) {
        deactivateProductVariantUseCase.execute(productPublicId, variantPublicId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ProductVariantResponse> increase(
            UUID productPublicId, UUID variantPublicId, UpdateVariantStockRequest request) {
        return ResponseEntity.ok(
                increaseProductVariantStockUseCase.execute(productPublicId, variantPublicId, request));
    }

    @Override
    public ResponseEntity<ProductVariantResponse> decrease(
            UUID productPublicId, UUID variantPublicId, UpdateVariantStockRequest request) {
        return ResponseEntity.ok(
                decreaseProductVariantUseCase.execute(productPublicId, variantPublicId, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID productPublicId, UUID variantPublicId) {
        removeProductVariantUseCase.execute(productPublicId, variantPublicId);
        return ResponseEntity.noContent().build();
    }
}