package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.CategoryDetailsResponse;
import com.javacore.spring_api_luvine.product.application.dto.CreateCategoryRequest;
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

@Slf4j
@UseCase
@RequiredArgsConstructor
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryDetailsResponse execute(CreateCategoryRequest request) {
        log.info("event=create_category_attempt");

        CategoryName categoryName = new CategoryName(request.categoryName());
        Description description = new Description(request.description());
        Slug slug = new Slug(request.categoryName());

        if (categoryRepository.existsBySlug(slug) || categoryRepository.existsByCategoryName(categoryName)) {
            log.warn("event=create_category_rejected reason=name_already_exists");
            throw new CategoryAlreadyExistsException();
        }

        Category parent = null;

        if (request.parentPublicId() != null) {
            parent = categoryRepository.findByPublicId(request.parentPublicId())
                    .orElseThrow(() -> {
                        log.warn("event=create_category_rejected reason=parent_not_found parentId={}",
                                request.parentPublicId());
                        return new CategoryNotFoundException();
                    });
        }

        Category newCategory = Category.create(parent, categoryName, description);

        categoryRepository.save(newCategory);

        log.info("event=create_category_completed publicId={}", newCategory.getPublicId());
        return categoryMapper.toCategoryDetailsResponse(newCategory);
    }
}