package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.CreateImageRequest;
import com.javacore.spring_api_luvine.product.application.dto.ProductImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.UploadResult;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductImage;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.AltText;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import com.javacore.spring_api_luvine.product.infrastructure.storage.StorageService;
import com.javacore.spring_api_luvine.product.infrastructure.storage.validation.FileImageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class CreateProductImageUseCase {

    private final ProductRepository productRepository;
    private final FileImageValidator imageValidator;
    private final StorageService storageService;
    private final ProductMapper productMapper;

    @Transactional
    public ProductImageResponse execute(
            UUID productPublicId, UUID variantPublicId,
            MultipartFile file, CreateImageRequest request) {
        log.info("event=create_product_image_attempt productId={} variantId={}", productPublicId, variantPublicId);

        imageValidator.validate(file);

        Product product = productRepository.findByPublicId(productPublicId)
                .orElseThrow(() -> {
                    log.warn("event=create_product_image_rejected reason=product_not_found productId={}",
                            productPublicId);
                    return new ProductNotFoundException();
                });

        ProductVariant variant = product.findVariantByPublicId(variantPublicId);

        String folder = String.format("products/%s/variants/%s/images", product.getPublicId(), variant.getPublicId());
        UUID imagePublicId = UUID.randomUUID();

        UploadResult uploadResult = null;

        try {
            uploadResult = storageService.upload(file, folder, imagePublicId.toString());

            ProductImage productImage = ProductImage.create(
                    uploadResult.imageUrl(),
                    uploadResult.storageKey(),
                    new AltText(request.altText()),
                    request.primaryImage()
            );

            variant.addImage(productImage, request.displayOrder());

            productRepository.saveAndFlush(product);

            log.info("event=create_product_image_completed productId={} variantId={} imageId={}",
                    productPublicId, variantPublicId, imagePublicId);
            return productMapper.toProductImageResponse(productImage);
        } catch (Exception ex) {
            if (uploadResult != null) {
                try {
                    storageService.delete(uploadResult.storageKey());
                } catch (Exception rollbackEx) {
                    log.error("event=create_product_image_rollback_failed productId={} variantId={} imageId={}",
                            productPublicId, variantPublicId, imagePublicId, rollbackEx);
                }
            }
            log.error("event=create_product_image_error productId={} variantId={}",
                    productPublicId, variantPublicId, ex);
            throw ex;
        }
    }
}