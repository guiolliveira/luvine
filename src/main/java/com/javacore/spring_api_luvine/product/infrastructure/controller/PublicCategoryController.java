package com.javacore.spring_api_luvine.product.infrastructure.controller;

import com.javacore.spring_api_luvine.product.application.dto.CategoryDetailsResponse;
import com.javacore.spring_api_luvine.product.application.dto.CategorySummaryResponse;
import com.javacore.spring_api_luvine.product.application.dto.SearchCategoryRequest;
import com.javacore.spring_api_luvine.product.application.usecase.GetCategoryDetailsUseCase;
import com.javacore.spring_api_luvine.product.application.usecase.SearchCategoryUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@PreAuthorize("hasAnyRole('CUSTOMER')")
public class PublicCategoryController {

    private final GetCategoryDetailsUseCase getCategoryDetailsUseCase;
    private final SearchCategoryUseCase searchCategoryUseCase;

    @GetMapping("/{categoryPublicId}")
    public ResponseEntity<CategoryDetailsResponse> getCategoryDetails(@PathVariable UUID categoryPublicId) {
        CategoryDetailsResponse response = getCategoryDetailsUseCase.execute(categoryPublicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<CategorySummaryResponse>> searchProduct(
            @ParameterObject @Valid SearchCategoryRequest request,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) @ParameterObject Pageable pageable) {
        Page<CategorySummaryResponse> response = searchCategoryUseCase.execute(request, pageable);
        return ResponseEntity.ok(response);
    }
}