package com.javacore.spring_api_luvine.product.infrastructure.controller;

import com.javacore.spring_api_luvine.product.application.dto.*;
import com.javacore.spring_api_luvine.product.application.usecase.*;
import com.javacore.spring_api_luvine.product.infrastructure.doc.AdminCategoryDoc;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/categories")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AdminCategoryController implements AdminCategoryDoc {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final ActivateCategoryUseCase activateCategoryUseCase;
    private final DeactivateCategoryUseCase deactivateCategoryUseCase;
    private final GetAdminCategoryDetailsUseCase getAdminCategoryDetailsUseCase;
    private final SearchAdminCategoryUseCase searchCategoryUseCase;

    @Override
    public ResponseEntity<CategoryDetailsResponse> create(CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createCategoryUseCase.execute(request));
    }

    @Override
    public ResponseEntity<CategoryDetailsResponse> update(UUID categoryPublicId, UpdateCategoryRequest request) {
        return ResponseEntity.ok(updateCategoryUseCase.execute(categoryPublicId, request));
    }

    @Override
    public ResponseEntity<Void> activate(UUID categoryPublicId) {
        activateCategoryUseCase.execute(categoryPublicId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deactivate(UUID categoryPublicId) {
        deactivateCategoryUseCase.execute(categoryPublicId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CategoryDetailsResponse> getCategoryDetails(UUID categoryPublicId) {
        return ResponseEntity.ok(getAdminCategoryDetailsUseCase.execute(categoryPublicId));
    }

    @Override
    public ResponseEntity<Page<CategorySummaryResponse>> searchProduct(
            SearchCategoryRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(searchCategoryUseCase.execute(request, pageable));
    }
}