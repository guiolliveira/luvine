package com.javacore.spring_api_luvine.product.infrastructure.controller;

import com.javacore.spring_api_luvine.product.application.dto.*;
import com.javacore.spring_api_luvine.product.application.usecase.*;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/categories")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AdminCategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final ActivateCategoryUseCase activateCategoryUseCase;
    private final DeactivateCategoryUseCase deactivateCategoryUseCase;
    private final GetAdminCategoryDetailsUseCase getAdminCategoryDetailsUseCase;
    private final SearchAdminCategoryUseCase searchCategoryUseCase;

    @PostMapping
    public ResponseEntity<CategoryDetailsResponse> create(@RequestBody @Valid CreateCategoryRequest request) {
        CategoryDetailsResponse response = createCategoryUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{categoryPublicId}")
    public ResponseEntity<CategoryDetailsResponse> update(
            @PathVariable UUID categoryPublicId,
            @RequestBody @Valid UpdateCategoryRequest request) {
        CategoryDetailsResponse response = updateCategoryUseCase.execute(categoryPublicId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{categoryPublicId}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID categoryPublicId) {
        activateCategoryUseCase.execute(categoryPublicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{categoryPublicId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID categoryPublicId) {
        deactivateCategoryUseCase.execute(categoryPublicId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{categoryPublicId}")
    public ResponseEntity<CategoryDetailsResponse> getCategoryDetails(@PathVariable UUID categoryPublicId) {
        CategoryDetailsResponse response = getAdminCategoryDetailsUseCase.execute(categoryPublicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<CategorySummaryResponse>> searchProduct(
            @ParameterObject @Valid SearchCategoryRequest request,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject Pageable pageable) {
        Page<CategorySummaryResponse> response = searchCategoryUseCase.execute(request, pageable);
        return ResponseEntity.ok(response);
    }
}