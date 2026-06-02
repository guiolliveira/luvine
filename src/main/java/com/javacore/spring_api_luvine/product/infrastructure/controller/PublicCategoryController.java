package com.javacore.spring_api_luvine.product.infrastructure.controller;

import com.javacore.spring_api_luvine.product.application.dto.CategoryDetailsResponse;
import com.javacore.spring_api_luvine.product.application.dto.CategorySummaryResponse;
import com.javacore.spring_api_luvine.product.application.dto.SearchCategoryRequest;
import com.javacore.spring_api_luvine.product.application.usecase.GetCategoryDetailsUseCase;
import com.javacore.spring_api_luvine.product.application.usecase.SearchCategoryUseCase;
import com.javacore.spring_api_luvine.product.infrastructure.doc.PublicCategoryDoc;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@PreAuthorize("hasAnyRole('CUSTOMER')")
public class PublicCategoryController implements PublicCategoryDoc {

    private final GetCategoryDetailsUseCase getCategoryDetailsUseCase;
    private final SearchCategoryUseCase searchCategoryUseCase;

    @Override
    public ResponseEntity<CategoryDetailsResponse> getCategoryDetails(UUID categoryPublicId) {
        return ResponseEntity.ok(getCategoryDetailsUseCase.execute(categoryPublicId));
    }

    @Override
    public ResponseEntity<Page<CategorySummaryResponse>> searchProduct(
            SearchCategoryRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(searchCategoryUseCase.execute(request, pageable));
    }
}