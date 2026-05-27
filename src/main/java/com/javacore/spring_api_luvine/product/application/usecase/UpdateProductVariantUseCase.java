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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class UpdateProductVariantUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductVariantResponse execute(UUID productPublicId, UUID variantPublicId, UpdateVariantRequest request) {
        Product product = productRepository.findByPublicId(productPublicId)
                .orElseThrow(ProductNotFoundException::new);

        ProductVariant variant = product.findVariantByPublicId(variantPublicId);

        if (request.newColor() != null || request.newSize() != null) {

            Color color = request.newColor() != null ? new Color(request.newColor()) : variant.getColor();
            Size size = request.newSize() != null ? new Size(request.newSize()) : variant.getSize();

            variant = product.updateVariantAttributes(variantPublicId, color, size);
        }

        if (request.newPrice() != null) {
            variant.changePrice(new Money(request.newPrice()));
        }

        return productMapper.toProductVariantResponse(variant);
    }
}