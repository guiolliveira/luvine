package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.CategoryImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.CreateCategoryImageRequest;
import com.javacore.spring_api_luvine.product.application.dto.UploadResult;
import com.javacore.spring_api_luvine.product.application.mapper.CategoryMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.entity.CategoryImage;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.AltText;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import com.javacore.spring_api_luvine.product.infrastructure.storage.StorageService;
import com.javacore.spring_api_luvine.product.infrastructure.storage.validation.FileImageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class CreateCategoryImageUseCase {

    private final CategoryRepository categoryRepository;
    private final StorageService storageService;
    private final FileImageValidator imageValidator;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryImageResponse execute(
            UUID categoryPublicId, MultipartFile file, CreateCategoryImageRequest request) {
        log.info("event=create_category_image_attempt categoryId={}", categoryPublicId);

        imageValidator.validate(file);

        Category category = categoryRepository.findByPublicId(categoryPublicId)
                .orElseThrow(() -> {
                    log.warn("event=create_category_image_rejected reason=category_not_found categoryId={}",
                            categoryPublicId);
                    return new CategoryNotFoundException();
                });

        CategoryImage oldImage = category.getImage();

        String folder = String.format("categories/%s/images", category.getPublicId());
        UUID imagePublicId = UUID.randomUUID();

        UploadResult uploadResult = null;

        try {
            uploadResult = storageService.upload(file, folder, imagePublicId.toString());

            CategoryImage categoryImage = CategoryImage.create(
                    uploadResult.imageUrl(),
                    uploadResult.storageKey(),
                    new AltText(request.altText())
            );

            category.addCategoryImage(categoryImage);

            categoryRepository.saveAndFlush(category);

            if (oldImage != null) {
                storageService.delete(oldImage.getStorageKey());
            }

            log.info("event=create_category_image_completed categoryId={} imageId={}", categoryPublicId, imagePublicId);
            return categoryMapper.toCategoryImageResponse(categoryImage);
        } catch (Exception ex) {
            if (uploadResult != null) {
                try {
                    storageService.delete(uploadResult.storageKey());
                } catch (Exception rollbackEx) {
                    log.error("event=create_category_image_rollback_failed categoryId={} imageId={}",
                            categoryPublicId, imagePublicId, rollbackEx);
                }
            }
            log.error("event=create_category_image_error categoryId={}", categoryPublicId, ex);
            throw ex;
        }
    }
}