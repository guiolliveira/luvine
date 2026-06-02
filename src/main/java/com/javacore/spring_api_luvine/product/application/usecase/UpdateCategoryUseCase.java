package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.CategoryDetailsResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateCategoryRequest;
import com.javacore.spring_api_luvine.product.application.mapper.CategoryMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryAlreadyExistsException;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.CategoryName;
import com.javacore.spring_api_luvine.product.domain.valueObject.Description;
import com.javacore.spring_api_luvine.product.domain.valueObject.Slug;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryDetailsResponse execute(UUID categoryPublicId, UpdateCategoryRequest request) {
        log.info("event=update_category_attempt publicId={}", categoryPublicId);

        Category category = categoryRepository.findByPublicId(categoryPublicId)
                .orElseThrow(() -> {
                    log.warn("event=update_category_rejected reason=category_not_found publicId={}", categoryPublicId);
                    return new CategoryNotFoundException();
                });

        if (request.newParentPublicId() != null) {
            Category parent = categoryRepository.findByPublicId(request.newParentPublicId())
                    .orElseThrow(() -> {
                        log.warn("event=update_category_rejected reason=parent_not_found publicId={} parentId={}",
                                categoryPublicId, request.newParentPublicId());
                        return new CategoryNotFoundException();
                    });

            category.changeParent(parent);
        }

        if (request.newCategoryName() != null && !request.newCategoryName().isBlank()) {
            CategoryName newCategoryName = new CategoryName(request.newCategoryName());
            Slug newSlug = new Slug(request.newCategoryName());

            if (categoryRepository.existsBySlugAndIdNot(newSlug, category.getId()) ||
                    categoryRepository.existsByCategoryNameAndIdNot(newCategoryName, category.getId())) {
                log.warn("event=update_category_rejected reason=name_already_exists publicId={}", categoryPublicId);
                throw new CategoryAlreadyExistsException();
            }

            category.rename(newCategoryName);
        }

        if (request.newDescription() != null && !request.newDescription().isBlank()) {
            category.changeDescription(new Description(request.newDescription()));
        }

        log.info("event=update_category_completed publicId={}", categoryPublicId);
        return categoryMapper.toCategoryDetailsResponse(category);
    }
}