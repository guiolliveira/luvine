package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.ProductImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateImageAltTextRequest;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductImage;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.AltText;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class UpdateProductImageAltTextUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductImageResponse execute(
            UUID productPublicId, UUID variantPublicId,
            UUID imagePublicId, UpdateImageAltTextRequest request) {
        Product product = productRepository.findByPublicId(productPublicId)
                .orElseThrow(ProductNotFoundException::new);

        ProductVariant variant = product.findVariantByPublicId(variantPublicId);

        ProductImage image = variant.findImageByPublicId(imagePublicId);

        if (request.newAltText() != null && !request.newAltText().isBlank()) {
            image.changeAltText(new AltText(request.newAltText()));
        }

        return productMapper.toProductImageResponse(image);
    }
}