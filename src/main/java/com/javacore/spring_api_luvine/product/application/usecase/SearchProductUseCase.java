package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.ProductSummaryResponse;
import com.javacore.spring_api_luvine.product.application.dto.SearchProductRequest;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.valueObject.*;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import com.javacore.spring_api_luvine.product.infrastructure.specification.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class SearchProductUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> execute(SearchProductRequest request, Pageable pageable) {
        Specification<Product> specification = ProductSpecifications.isVisible();

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

        if (request.status() != null) {
            specification = specification.and(ProductSpecifications.hasStatus(request.status()));
        }

        Page<Product> products = productRepository.findAll(specification, pageable);

        return products.map(productMapper::toProductSummaryResponse);
    }
}