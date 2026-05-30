package com.javacore.spring_api_luvine.product.infrastructure.controller;

import com.javacore.spring_api_luvine.product.application.dto.CategoryImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.CreateCategoryImageRequest;
import com.javacore.spring_api_luvine.product.application.dto.UpdateImageAltTextRequest;
import com.javacore.spring_api_luvine.product.application.usecase.CreateCategoryImageUseCase;
import com.javacore.spring_api_luvine.product.application.usecase.UpdateCategoryImageAltTextUseCase;
import jakarta.validation.Valid;
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
public class AdminCategoryImageController {

    private final CreateCategoryImageUseCase createCategoryImageUseCase;
    private final UpdateCategoryImageAltTextUseCase updateCategoryImageAltTextUseCase;

    @PostMapping("{categoryPublicId}/image")
    public ResponseEntity<CategoryImageResponse> create(
            @PathVariable UUID categoryPublicId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("data") @Valid CreateCategoryImageRequest request) {
        CategoryImageResponse response = createCategoryImageUseCase.execute(categoryPublicId, file, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{categoryPublicId}/image/{imagePublicId}")
    public ResponseEntity<CategoryImageResponse> update(
            @PathVariable UUID categoryPublicId,
            @PathVariable UUID imagePublicId,
            UpdateImageAltTextRequest request) {
        CategoryImageResponse response =
                updateCategoryImageAltTextUseCase.execute(categoryPublicId, imagePublicId, request);
        return ResponseEntity.ok(response);
    }
}