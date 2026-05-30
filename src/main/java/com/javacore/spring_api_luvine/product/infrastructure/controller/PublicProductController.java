package com.javacore.spring_api_luvine.product.infrastructure.controller;

import com.javacore.spring_api_luvine.product.application.dto.ProductDetailsResult;
import com.javacore.spring_api_luvine.product.application.dto.ProductSummaryResponse;
import com.javacore.spring_api_luvine.product.application.dto.SearchProductRequest;
import com.javacore.spring_api_luvine.product.application.usecase.GetProductDetailsUseCase;
import com.javacore.spring_api_luvine.product.application.usecase.SearchProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/catalog/products")
@PreAuthorize("hasAnyRole('CUSTOMER')")
public class PublicProductController {

    private final GetProductDetailsUseCase getProductDetailsUseCase;
    private final SearchProductUseCase searchProductUseCase;

    @GetMapping("/{slug}-{productPublicId}")
    public ResponseEntity<?> getProductDetails(
            @PathVariable UUID productPublicId,
            @PathVariable String slug) {
        ProductDetailsResult result = getProductDetailsUseCase.execute(productPublicId);

        if (!result.currentSlug().equals(slug)) {
            URI uri = URI.create(
                    "/api/v1/catalog/products/" + result.currentSlug() + "-" + productPublicId
            );

            return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT).location(uri).build();
        }

        return ResponseEntity.ok(result.response());
    }

    @GetMapping
    public ResponseEntity<Page<ProductSummaryResponse>> searchProduct(
            @ParameterObject SearchProductRequest request,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) @ParameterObject Pageable pageable) {
        Page<ProductSummaryResponse> response = searchProductUseCase.execute(request, pageable);
        return ResponseEntity.ok(response);
    }
}