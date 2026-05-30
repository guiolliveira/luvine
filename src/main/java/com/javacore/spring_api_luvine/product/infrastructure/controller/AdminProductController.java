package com.javacore.spring_api_luvine.product.infrastructure.controller;

import com.javacore.spring_api_luvine.product.application.dto.*;
import com.javacore.spring_api_luvine.product.application.usecase.CreateProductUseCase;
import com.javacore.spring_api_luvine.product.application.usecase.GetAdminProductDetailsUseCase;
import com.javacore.spring_api_luvine.product.application.usecase.SearchAdminProductUseCase;
import com.javacore.spring_api_luvine.product.application.usecase.UpdateProductUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/admin/catalog/products")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN')")
public class AdminProductController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final GetAdminProductDetailsUseCase getAdminProductDetailsUseCase;
    private final SearchAdminProductUseCase searchAdminProductUseCase;

    @PostMapping
    public ResponseEntity<ProductDetailsResponse> create(@RequestBody @Valid CreateProductRequest request) {
        ProductDetailsResponse response = createProductUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{productPublicId}")
    public ResponseEntity<ProductDetailsResponse> update(
            @PathVariable UUID productPublicId,
            @RequestBody @Valid UpdateProductRequest request) {
        ProductDetailsResponse response = updateProductUseCase.execute(productPublicId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productPublicId}")
    public ResponseEntity<ProductDetailsResponse> getProductsDetails(@PathVariable UUID productPublicId) {
        ProductDetailsResponse response = getAdminProductDetailsUseCase.execute(productPublicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProductSummaryResponse>> searchProduct(
            @ParameterObject @Valid SearchProductRequest request,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) @ParameterObject Pageable pageable) {
        Page<ProductSummaryResponse> response = searchAdminProductUseCase.execute(request, pageable);
        return ResponseEntity.ok(response);
    }
}