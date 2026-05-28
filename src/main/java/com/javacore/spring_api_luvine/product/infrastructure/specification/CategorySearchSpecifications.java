package com.javacore.spring_api_luvine.product.infrastructure.specification;

import com.javacore.spring_api_luvine.product.application.dto.SearchCategoryRequest;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.valueObject.CategoryName;
import com.javacore.spring_api_luvine.product.domain.valueObject.Slug;
import org.springframework.data.jpa.domain.Specification;

public final class CategorySearchSpecifications {

    private CategorySearchSpecifications() {}

    public static Specification<Category> build(SearchCategoryRequest request) {
        Specification<Category> specification = (root, query, cb) -> cb.conjunction();

        if (request.categoryName() != null && !request.categoryName().isBlank()) {
            specification = specification
                    .and(CategorySpecifications.nameContains(new CategoryName(request.categoryName())));
        }

        if (request.parentSlug() != null && !request.parentSlug().isBlank()) {
            specification = specification.and(CategorySpecifications.hasParent(new Slug(request.parentSlug())));
        }

        if (request.rootOnly() != null && request.rootOnly()) {
            specification = specification.and(CategorySpecifications.isRootCategory());
        }

        if (request.active() != null) {
            specification = specification.and(CategorySpecifications.hasActive(request.active()));
        }

        return specification;
    }
}