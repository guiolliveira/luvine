package com.javacore.spring_api_luvine.product.infrastructure.controller;

import com.javacore.spring_api_luvine.product.application.dto.CreateVariantRequest;
import com.javacore.spring_api_luvine.product.application.dto.ProductVariantResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateVariantRequest;
import com.javacore.spring_api_luvine.product.application.dto.UpdateVariantStockRequest;
import com.javacore.spring_api_luvine.product.application.usecase.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/catalog/products")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AdminProductVariantController {

    private final CreateProductVariantUseCase createProductVariantUseCase;
    private final UpdateProductVariantUseCase updateProductVariantUseCase;
    private final ActivateProductVariantUseCase activateProductVariantUseCase;
    private final DeactivateProductVariantUseCase deactivateProductVariantUseCase;
    private final IncreaseProductVariantStockUseCase increaseProductVariantStockUseCase;
    private final DecreaseProductVariantUseCase decreaseProductVariantUseCase;
    private final RemoveProductVariantUseCase removeProductVariantUseCase;

    @PostMapping("/{productPublicId}/variants}")
    public ResponseEntity<ProductVariantResponse> create(
            @PathVariable UUID productPublicId,
            @RequestBody @Valid CreateVariantRequest request) {
        ProductVariantResponse response = createProductVariantUseCase.execute(productPublicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{productPublicId}/variants/{variantPublicId}")
    public ResponseEntity<ProductVariantResponse> update(
            @PathVariable UUID productPublicId,
            @PathVariable UUID variantPublicId,
            @RequestBody @Valid UpdateVariantRequest request) {
        ProductVariantResponse response =
                updateProductVariantUseCase.execute(productPublicId, variantPublicId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productPublicId}/variants/{variantPublicId}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable UUID productPublicId,
            @PathVariable UUID variantPublicId) {
        activateProductVariantUseCase.execute(productPublicId, variantPublicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productPublicId}/variants/{variantPublicId}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID productPublicId,
            @PathVariable UUID variantPublicId) {
        deactivateProductVariantUseCase.execute(productPublicId, variantPublicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productPublicId}/variants/{variantPublicId}/stock/increase")
    public ResponseEntity<ProductVariantResponse> increase(
            @PathVariable UUID productPublicId,
            @PathVariable UUID variantPublicId,
            @RequestBody @Valid UpdateVariantStockRequest request) {
        ProductVariantResponse response =
                increaseProductVariantStockUseCase.execute(productPublicId, variantPublicId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productPublicId}/variants/{variantPublicId}/stock/decrease")
    public ResponseEntity<ProductVariantResponse> decrease(
            @PathVariable UUID productPublicId,
            @PathVariable UUID variantPublicId,
            @RequestBody @Valid UpdateVariantStockRequest request) {
        ProductVariantResponse response =
                decreaseProductVariantUseCase.execute(productPublicId, variantPublicId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productPublicId}/variants/{variantPublicId}")
    public ResponseEntity<Void> delete(@PathVariable UUID productPublicId, @PathVariable UUID variantPublicId) {
        removeProductVariantUseCase.execute(productPublicId, variantPublicId);
        return ResponseEntity.noContent().build();
    }
}