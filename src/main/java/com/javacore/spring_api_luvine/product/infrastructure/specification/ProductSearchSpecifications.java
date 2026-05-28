package com.javacore.spring_api_luvine.product.infrastructure.specification;

import com.javacore.spring_api_luvine.product.application.dto.SearchProductRequest;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.valueObject.*;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSearchSpecifications {

    private ProductSearchSpecifications() {}

    public static Specification<Product> build(SearchProductRequest request, boolean includeInactive) {
        Specification<Product> specification = includeInactive ?
                ProductSpecifications.all() : ProductSpecifications.isVisible();

        if (request.categorySlug() != null) {
            specification = specification.and(ProductSpecifications.hasCategory(new Slug(request.categorySlug())));
        }

        if (request.productName() != null && !request.productName().isBlank()) {
            specification = specification
                    .and(ProductSpecifications.nameContains(new ProductName(request.productName())));
        }

        if (request.color() != null && !request.color().isBlank()) {
            specification = specification.and(ProductSpecifications.hasColor(new Color(request.color())));
        }

        if (request.size() != null && !request.size().isBlank()) {
            specification = specification.and(ProductSpecifications.hasSize(new Size(request.size())));
        }

        if (request.minPrice() != null) {
            specification = specification
                    .and(ProductSpecifications.basePriceGreaterThanOrEqualTo(new Money(request.minPrice())));
        }

        if (request.maxPrice() != null) {
            specification = specification
                    .and(ProductSpecifications.basePriceLessThanOrEqualTo(new Money(request.maxPrice())));
        }

        if (includeInactive && request.status() != null) {
            specification = specification.and(ProductSpecifications.hasStatus(request.status()));
        }

        return specification;
    }
}