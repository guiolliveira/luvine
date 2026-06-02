package com.javacore.spring_api_luvine.product.infrastructure.controller;

import com.javacore.spring_api_luvine.product.application.dto.CategoryImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.CreateCategoryImageRequest;
import com.javacore.spring_api_luvine.product.application.dto.UpdateImageAltTextRequest;
import com.javacore.spring_api_luvine.product.application.usecase.CreateCategoryImageUseCase;
import com.javacore.spring_api_luvine.product.application.usecase.UpdateCategoryImageAltTextUseCase;
import com.javacore.spring_api_luvine.product.infrastructure.doc.AdminCategoryImageDoc;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/categories")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AdminCategoryImageController implements AdminCategoryImageDoc {

    private final CreateCategoryImageUseCase createCategoryImageUseCase;
    private final UpdateCategoryImageAltTextUseCase updateCategoryImageAltTextUseCase;

    @Override
    public ResponseEntity<CategoryImageResponse> create(
            UUID categoryPublicId, MultipartFile file, CreateCategoryImageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createCategoryImageUseCase.execute(categoryPublicId, file, request));
    }

    @Override
    public ResponseEntity<CategoryImageResponse> update(
            UUID categoryPublicId, UUID imagePublicId, UpdateImageAltTextRequest request) {
        return ResponseEntity.ok(
                updateCategoryImageAltTextUseCase.execute(categoryPublicId, imagePublicId, request));
    }
}