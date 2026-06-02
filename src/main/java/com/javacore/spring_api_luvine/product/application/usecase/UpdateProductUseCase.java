package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.ProductDetailsResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateProductRequest;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryInactiveException;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.domain.exception.ProductAlreadyExistsException;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.Description;
import com.javacore.spring_api_luvine.product.domain.valueObject.Money;
import com.javacore.spring_api_luvine.product.domain.valueObject.ProductName;
import com.javacore.spring_api_luvine.product.domain.valueObject.Slug;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductDetailsResponse execute(UUID productPublicId, UpdateProductRequest request) {
        log.info("event=update_product_attempt publicId={}", productPublicId);

        Product product = productRepository.findByPublicId(productPublicId)
                .orElseThrow(() -> {
                    log.warn("event=update_product_rejected reason=product_not_found publicId={}", productPublicId);
                    return new ProductNotFoundException();
                });

        if (request.newCategorySlug() != null && !request.newCategorySlug().isBlank()) {
            Category category = categoryRepository.findBySlug(new Slug(request.newCategorySlug()))
                    .orElseThrow(() -> {
                        log.warn("event=update_product_rejected reason=category_not_found publicId={}", productPublicId);
                        return new CategoryNotFoundException();
                    });

            if (!category.isActive()) {
                log.warn("event=update_product_rejected reason=category_inactive publicId={} categoryId={}",
                        productPublicId, category.getPublicId());
                throw new CategoryInactiveException();
            }

            product.changeCategory(category);
        }

        if (request.newProductName() != null && !request.newProductName().isBlank()) {
            ProductName newProductName = new ProductName(request.newProductName());
            Slug newSlug = new Slug(request.newProductName());

            if (productRepository.existsByProductNameAndIdNot(newProductName, product.getId())
                    || productRepository.existsBySlugAndIdNot(newSlug, product.getId())) {
                log.warn("event=update_product_rejected reason=name_already_exists publicId={}", productPublicId);
                throw new ProductAlreadyExistsException();
            }

            product.rename(newProductName);
        }

        if (request.newDescription() != null && !request.newDescription().isBlank()) {
            product.changeDescription(new Description(request.newDescription()));
        }

        if (request.newBasePrice() != null) {
            product.changeBasePrice(new Money(request.newBasePrice()));
        }

        product.changeStatus(request.newStatus());

        log.info("event=update_product_completed publicId={}", productPublicId);
        return productMapper.toProductDetailsResponse(product);
    }
}