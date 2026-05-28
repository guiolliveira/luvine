package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.CategoryDetailsResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateCategoryRequest;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryAlreadyExistsException;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.CategoryName;
import com.javacore.spring_api_luvine.product.domain.valueObject.Description;
import com.javacore.spring_api_luvine.product.domain.valueObject.Slug;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Transactional
    public CategoryDetailsResponse execute(UUID categoryPublicId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findByPublicId(categoryPublicId)
                .orElseThrow(CategoryNotFoundException::new);

        if (request.newParentPublicId() != null) {
            Category parent = categoryRepository.findByPublicId(request.newParentPublicId())
                    .orElseThrow(CategoryNotFoundException::new);

            category.changeParent(parent);
        }

        if (request.newCategoryName() != null && !request.newCategoryName().isBlank()) {
            CategoryName newCategoryName = new CategoryName(request.newCategoryName());
            Slug newSlug = new Slug(request.newCategoryName());

            if (categoryRepository.existsBySlug(newSlug) ||
                    categoryRepository.existsByCategoryNameAndIdNot(newCategoryName, category.getId())) {
                throw new CategoryAlreadyExistsException();
            }

            category.rename(newCategoryName);
        }

        if (request.newDescription() != null && !request.newDescription().isBlank()) {
            category.changeDescription(new Description(request.newDescription()));
        }

        return productMapper.toCategoryDetailsResponse(category);
    }
}