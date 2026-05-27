package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.CreateVariantRequest;
import com.javacore.spring_api_luvine.product.application.dto.ProductVariantResponse;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.*;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class CreateProductVariantUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductVariantResponse execute(UUID productPublicId, CreateVariantRequest request) {
        Product product = productRepository.findByPublicId(productPublicId)
                .orElseThrow(ProductNotFoundException::new);

        ProductVariant variant = ProductVariant.create(
                new Sku(request.sku()),
                new Color(request.color()),
                new Size(request.size()),
                new Money(request.price()),
                new StockQuantity(request.stockQuantity())
        );

        product.addVariant(variant);

        return productMapper.toProductVariantResponse(variant);
    }
}