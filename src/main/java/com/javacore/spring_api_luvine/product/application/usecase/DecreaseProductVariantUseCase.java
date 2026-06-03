package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.ProductVariantResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateVariantStockRequest;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.StockQuantity;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class DecreaseProductVariantUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductVariantResponse execute(
            UUID productPublicId, UUID variantPublicId, UpdateVariantStockRequest request) {
        log.info("event=decrease_stock_attempt productId={} variantId={}", productPublicId, variantPublicId);

        Product product = productRepository.findByPublicId(productPublicId)
                .orElseThrow(() -> {
                    log.warn("event=decrease_stock_rejected reason=product_not_found productId={}", productPublicId);
                    return new ProductNotFoundException();
                });

        ProductVariant variant = product.findVariantByPublicId(variantPublicId);

        variant.decreaseStock(new StockQuantity(request.stockQuantity()));

        log.info("event=decrease_stock_completed productId={} variantId={}", productPublicId, variantPublicId);
        return productMapper.toProductVariantResponse(variant);
    }
}