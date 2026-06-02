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
public class ActivateProductVariantUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public void execute(UUID productPublicId, UUID variantPublicId) {
        log.info("event=activate_product_variant_attempt productId={} variantId={}", productPublicId, variantPublicId);

        Product product = productRepository.findByPublicId(productPublicId)
                .orElseThrow(() -> {
                    log.warn("event=activate_product_variant_rejected reason=product_not_found productId={}",
                            productPublicId);
                    return new ProductNotFoundException();
                });

        ProductVariant variant = product.findVariantByPublicId(variantPublicId);

        variant.activate();

        log.info("event=activate_product_variant_completed productId={} variantId={}", productPublicId,
                variantPublicId);
    }
}