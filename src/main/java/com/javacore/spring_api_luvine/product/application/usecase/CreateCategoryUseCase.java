package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.CategoryDetailsResponse;
import com.javacore.spring_api_luvine.product.application.dto.CreateCategoryRequest;
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

@UseCase
@RequiredArgsConstructor
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Transactional
    public CategoryDetailsResponse execute(CreateCategoryRequest request) {
        CategoryName categoryName = new CategoryName(request.categoryName());
        Description description = new Description(request.description());
        Slug slug = new Slug(request.categoryName());

        if (categoryRepository.existsBySlug(slug) || categoryRepository.existsByCategoryName(categoryName)) {
            throw new CategoryAlreadyExistsException();
        }

        Category parent = null;

        if (request.parentPublicId() != null) {
            parent = categoryRepository.findByPublicId(request.parentPublicId())
                    .orElseThrow(CategoryNotFoundException::new);
        }

        Category newCategory = Category.create(
                parent,
                categoryName,
                description
        );

        categoryRepository.save(newCategory);

        return productMapper.toCategoryDetailsResponse(newCategory);
    }
}