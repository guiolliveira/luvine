package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.ProductImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.ReorderImageRequest;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class ReorderProductImageUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public List<ProductImageResponse> execute(UUID productPublicId, UUID variantPublicId, ReorderImageRequest request) {
        Product product = productRepository.findByPublicId(productPublicId)
                .orElseThrow(ProductNotFoundException::new);

        ProductVariant variant = product.findVariantByPublicId(variantPublicId);

        variant.reorderImages(request.imagePublicIds());

        return variant.getImages().stream()
                .map(productMapper::toProductImageResponse)
                .toList();
    }
}