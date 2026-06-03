package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.ProductSummaryResponse;
import com.javacore.spring_api_luvine.product.application.dto.SearchProductRequest;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import com.javacore.spring_api_luvine.product.infrastructure.specification.ProductSearchSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SearchAdminProductUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> execute(SearchProductRequest request, Pageable pageable) {
        log.info("event=search_admin_product_attempt page={} size={}", pageable.getPageNumber(),
                pageable.getPageSize());

        Specification<Product> specification = ProductSearchSpecifications.build(request);

        Page<Product> products = productRepository.findAll(specification, pageable);

        log.info("event=search_admin_product_completed totalElements={} totalPages={}",
                products.getTotalElements(), products.getTotalPages());
        return products.map(productMapper::toProductSummaryResponse);
    }
}