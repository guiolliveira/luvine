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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SearchCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public Page<CategorySummaryResponse> execute(SearchCategoryRequest request, Pageable pageable) {
        log.info("event=search_category_attempt page={} size={}", pageable.getPageNumber(), pageable.getPageSize());

        Specification<Category> specification = CategorySearchSpecifications.build(request)
                .and(CategorySpecifications.isActive());

        Page<Category> categories = categoryRepository.findAll(specification, pageable);

        log.info("event=search_category_completed totalElements={} totalPages={}",
                categories.getTotalElements(), categories.getTotalPages());
        return categories.map(categoryMapper::toCategorySummaryResponse);
    }
}