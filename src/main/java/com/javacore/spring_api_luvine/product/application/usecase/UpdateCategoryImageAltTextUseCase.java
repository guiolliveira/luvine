package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.CategoryImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateImageAltTextRequest;
import com.javacore.spring_api_luvine.product.application.mapper.CategoryMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.entity.CategoryImage;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.AltText;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UpdateCategoryImageAltTextUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryImageResponse execute(
            UUID categoryPublicId, UUID categoryImagePublicId, UpdateImageAltTextRequest request) {
        log.info("event=update_category_image_alt_text_attempt categoryId={} imageId={}",
                categoryPublicId, categoryImagePublicId);

        Category category = categoryRepository.findByPublicId(categoryPublicId)
                .orElseThrow(() -> {
                    log.warn("event=update_category_image_alt_text_rejected reason=category_not_found categoryId={}", categoryPublicId);
                    return new CategoryNotFoundException();
                });

        CategoryImage image = category.findImageByPublicId(categoryImagePublicId);

        image.changeAltText(new AltText(request.newAltText()));

        log.info("event=update_category_image_alt_text_completed categoryId={} imageId={}",
                categoryPublicId, categoryImagePublicId);
        return categoryMapper.toCategoryImageResponse(image);
    }
}