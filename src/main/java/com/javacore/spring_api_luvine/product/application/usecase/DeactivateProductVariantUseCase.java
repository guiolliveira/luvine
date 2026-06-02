package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
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
public class DeactivateProductVariantUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public void execute(UUID productId, UUID variantPublicId) {
        log.info("event=deactivate_product_variant_attempt productId={} variantId={}",
                productId, variantPublicId);

        Product product = productRepository.findByPublicId(productId)
                .orElseThrow(() -> {
                    log.warn("event=deactivate_product_variant_rejected reason=product_not_found productId={}",
                            productId);
                    return new ProductNotFoundException();
                });

        ProductVariant variant = product.findVariantByPublicId(variantPublicId);

        variant.deactivate();

        log.info("event=deactivate_product_variant_completed productId={} variantId={}", productId, variantPublicId);
    }
}