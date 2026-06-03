package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.ProductVariantResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateVariantRequest;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.Color;
import com.javacore.spring_api_luvine.product.domain.valueObject.Money;
import com.javacore.spring_api_luvine.product.domain.valueObject.Size;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UpdateProductVariantUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductVariantResponse execute(UUID productPublicId, UUID variantPublicId, UpdateVariantRequest request) {
        log.info("event=update_product_variant_attempt productId={} variantId={}", productPublicId, variantPublicId);

        Product product = productRepository.findByPublicId(productPublicId)
                .orElseThrow(() -> {
                    log.warn("event=update_product_variant_rejected reason=product_not_found productId={}",
                            productPublicId);
                    return new ProductNotFoundException();
                });

        ProductVariant variant = product.findVariantByPublicId(variantPublicId);

        boolean asNewColor = request.newColor() != null && !request.newColor().isBlank();
        boolean asNewSize = request.newSize() != null && !request.newSize().isBlank();

        if (asNewColor || asNewSize) {
            Color color = request.newColor() != null ? new Color(request.newColor()) : variant.getColor();
            Size size = request.newSize() != null ? new Size(request.newSize()) : variant.getSize();

            variant = product.updateVariantAttributes(variantPublicId, color, size);
        }

        if (request.newPrice() != null) {
            variant.changePrice(new Money(request.newPrice()));
        }

        log.info("event=update_product_variant_completed productId={} variantId={}", productPublicId, variantPublicId);
        return productMapper.toProductVariantResponse(variant);
    }
}