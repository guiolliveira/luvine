package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductImage;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import com.javacore.spring_api_luvine.product.infrastructure.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class RemoveProductImageUseCase {

    private final ProductRepository productRepository;
    private final StorageService storageService;

    @Transactional
    public void execute(UUID productPublicId, UUID variantPublicId, UUID imagePublicId) {
        Product product = productRepository.findByPublicId(productPublicId)
                .orElseThrow(ProductNotFoundException::new);

        ProductVariant variant = product.findVariantByPublicId(variantPublicId);

        ProductImage image = variant.findImageByPublicId(imagePublicId);

        try {
            storageService.delete(image.getStorageKey());
        } catch (Exception ex) {
            log.error(
                    "FAILED_TO_DELETE_IMAGE_FROM_STORAGE storageKey={}, productPublicId={}, variantPublicId={}, " +
                            "imagePublicId={}",
                    image.getStorageKey(),
                    productPublicId,
                    variantPublicId,
                    imagePublicId,
                    ex
            );
            throw ex;
        }

        variant.removeImage(image);
    }
}