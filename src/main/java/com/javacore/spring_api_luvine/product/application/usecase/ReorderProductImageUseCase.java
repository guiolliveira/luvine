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
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ReorderProductImageUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public List<ProductImageResponse> execute(UUID productPublicId, UUID variantPublicId, ReorderImageRequest request) {
        log.info("event=reorder_product_images_attempt productId={} variantId={}", productPublicId, variantPublicId);

        Product product = productRepository.findByPublicId(productPublicId)
                .orElseThrow(() -> {
                    log.warn("event=reorder_product_images_rejected reason=product_not_found productId={}",
                            productPublicId);
                    return new ProductNotFoundException();
                });

        ProductVariant variant = product.findVariantByPublicId(variantPublicId);

        variant.reorderImages(request.imagePublicIds());

        log.info("event=reorder_product_images_completed productId={} variantId={} imageCount={}",
                productPublicId, variantPublicId, variant.getImages().size());
        return variant.getImages().stream()
                .map(productMapper::toProductImageResponse)
                .toList();
    }
}