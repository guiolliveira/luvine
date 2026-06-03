package com.javacore.spring_api_luvine.product.infrastructure.controller;

import com.javacore.spring_api_luvine.product.application.dto.*;
import com.javacore.spring_api_luvine.product.application.usecase.CreateProductUseCase;
import com.javacore.spring_api_luvine.product.application.usecase.GetAdminProductDetailsUseCase;
import com.javacore.spring_api_luvine.product.application.usecase.SearchAdminProductUseCase;
import com.javacore.spring_api_luvine.product.application.usecase.UpdateProductUseCase;
import com.javacore.spring_api_luvine.product.infrastructure.doc.AdminProductDoc;
import lombok.RequiredArgsConstructor;
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
public class AdminProductController implements AdminProductDoc {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final GetAdminProductDetailsUseCase getAdminProductDetailsUseCase;
    private final SearchAdminProductUseCase searchAdminProductUseCase;

    @Override
    public ResponseEntity<ProductDetailsResponse> create(CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createProductUseCase.execute(request));
    }

    @Override
    public ResponseEntity<ProductDetailsResponse> update(UUID productPublicId, UpdateProductRequest request) {
        return ResponseEntity.ok(updateProductUseCase.execute(productPublicId, request));
    }

    @Override
    public ResponseEntity<ProductDetailsResponse> getProductsDetails(UUID productPublicId) {
        return ResponseEntity.ok(getAdminProductDetailsUseCase.execute(productPublicId));
    }

    @Override
    public ResponseEntity<Page<ProductSummaryResponse>> searchProduct(
            SearchProductRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(searchAdminProductUseCase.execute(request, pageable));
    }
}