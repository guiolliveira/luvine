package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.ProductImageResponse;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductImage;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SetPrimaryProductImageUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductImageResponse execute(UUID productPublicId, UUID variantPublicId, UUID imagePublicId) {
        log.info("event=set_primary_image_attempt productId={} variantId={} imageId={}",
                productPublicId, variantPublicId, imagePublicId);

        Product product = productRepository.findByPublicId(productPublicId)
                .orElseThrow(() -> {
                    log.warn("event=set_primary_image_rejected reason=product_not_found productId={}", productPublicId);
                    return new ProductNotFoundException();
                });

        ProductVariant variant = product.findVariantByPublicId(variantPublicId);

        ProductImage image = variant.findImageByPublicId(imagePublicId);

        variant.setPrimary(image);

        product.changeThumbnailUrl(product.resolveThumbnailUrl().orElse(null));

        log.info("event=set_primary_image_completed productId={} variantId={} imageId={}",
                productPublicId, variantPublicId, imagePublicId);
        return productMapper.toProductImageResponse(image);
    }
}