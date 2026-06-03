package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.entity.CategoryImage;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import com.javacore.spring_api_luvine.product.infrastructure.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class RemoveCategoryImageUseCase {

    private final CategoryRepository categoryRepository;
    private final StorageService storageService;

    @Transactional
    public void execute(UUID categoryPublicId, UUID imagePublicId) {
        log.info("event=remove_category_image_attempt categoryId={} imageId={}",
                categoryPublicId, imagePublicId);

        Category category = categoryRepository.findByPublicId(categoryPublicId)
                .orElseThrow(() -> {
                    log.warn("event=remove_category_image_rejected reason=category_not_found categoryId={}",
                            categoryPublicId);
                    return new CategoryNotFoundException();
                });

        CategoryImage image = category.findImageByPublicId(imagePublicId);

        try {
            storageService.delete(image.getStorageKey());
        } catch (Exception ex) {
            log.error("event=remove_category_image_storage_error categoryId={} imageId={} error={}",
                    categoryPublicId, imagePublicId, ex.getMessage());
            throw ex;
        }

        category.removeCategoryImage(image);

        log.info("event=remove_category_image_completed categoryId={} imageId={}",
                categoryPublicId, imagePublicId);
    }
}