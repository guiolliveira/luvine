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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class UpdateCategoryImageAltTextUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryImageResponse execute(
            UUID categoryPublicId, UUID categoryImagePublicId, UpdateImageAltTextRequest request) {
        Category category = categoryRepository.findByPublicId(categoryPublicId)
                .orElseThrow(CategoryNotFoundException::new);

        CategoryImage image = category.findImageByPublicId(categoryImagePublicId);

        image.changeAltText(new AltText(request.newAltText()));

        return categoryMapper.toCategoryImageResponse(image);
    }
}