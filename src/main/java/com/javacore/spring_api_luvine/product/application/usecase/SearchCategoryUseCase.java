package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.CategorySummaryResponse;
import com.javacore.spring_api_luvine.product.application.dto.SearchCategoryRequest;
import com.javacore.spring_api_luvine.product.application.mapper.CategoryMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import com.javacore.spring_api_luvine.product.infrastructure.specification.CategorySearchSpecifications;
import com.javacore.spring_api_luvine.product.infrastructure.specification.CategorySpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class SearchCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public Page<CategorySummaryResponse> execute(SearchCategoryRequest request, Pageable pageable) {
        Specification<Category> specification = CategorySearchSpecifications.build(request)
                .and(CategorySpecifications.isActive());

        Page<Category> products = categoryRepository.findAll(specification, pageable);

        return products.map(categoryMapper::toCategorySummaryResponse);
    }
}