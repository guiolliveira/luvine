package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.ProductDetailsResponse;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class GetAdminProductDetailsUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public ProductDetailsResponse execute(UUID productPublicId) {
        log.info("event=get_admin_product_details_attempt publicId={}", productPublicId);

        Product product = productRepository.findDetailsByPublicId(productPublicId)
                .orElseThrow(() -> {
                    log.warn("event=get_admin_product_details_rejected reason=product_not_found publicId={}",
                            productPublicId);
                    return new ProductNotFoundException();
                });

        log.info("event=get_admin_product_details_completed publicId={}", productPublicId);
        return productMapper.toProductDetailsResponse(product);
    }
}