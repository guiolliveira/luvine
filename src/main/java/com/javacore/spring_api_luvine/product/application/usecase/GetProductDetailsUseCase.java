package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.ProductDetailsResult;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.Status;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class GetProductDetailsUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public ProductDetailsResult execute(UUID publicId) {
        log.info("event=get_product_details_attempt publicId={}", publicId);

        Product product = productRepository.findDetailsByPublicIdAndStatus(publicId, Status.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("event=get_product_details_rejected reason=product_not_found publicId={}", publicId);
                    return new ProductNotFoundException();
                });

        log.info("event=get_product_details_completed publicId={} slug={}", publicId, product.getSlug().value());
        return new ProductDetailsResult(
                productMapper.toProductDetailsResponse(product),
                product.getSlug().value()
        );
    }
}