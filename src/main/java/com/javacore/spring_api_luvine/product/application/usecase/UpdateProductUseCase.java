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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductDetailsResponse execute(UUID productPublicId, UpdateProductRequest request) {
        Product product = productRepository.findDetailsByPublicId(productPublicId)
                .orElseThrow(ProductNotFoundException::new);

        if (request.newCategorySlug() != null && !request.newCategorySlug().isBlank()) {
            Category category = categoryRepository.findBySlug(new Slug(request.newCategorySlug()))
                    .orElseThrow(CategoryNotFoundException::new);

            if (!category.isActive()) {
                throw new CategoryInactiveException();
            }

            product.changeCategory(category);
        }

        if (request.newProductName() != null && !request.newProductName().isBlank()) {
            ProductName newProductName = new ProductName(request.newProductName());
            Slug newSlug = new Slug(request.newProductName());

            if (productRepository.existsByProductNameAndIdNot(newProductName, product.getId())
                    || productRepository.existsBySlugAndIdNot(newSlug, product.getId())) {
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

        if (request.newStatus() != null) {
            product.changeStatus(request.newStatus());
        }

        return productMapper.toProductDetailsResponse(product);
    }
}