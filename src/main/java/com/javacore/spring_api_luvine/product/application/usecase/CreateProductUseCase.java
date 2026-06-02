package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.CreateProductRequest;
import com.javacore.spring_api_luvine.product.application.dto.ProductDetailsResponse;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryInactiveException;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.domain.exception.EmptyProductVariantsException;
import com.javacore.spring_api_luvine.product.domain.exception.ProductAlreadyExistsException;
import com.javacore.spring_api_luvine.product.domain.valueObject.*;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductDetailsResponse execute(CreateProductRequest request) {
        ProductName productName = new ProductName(request.productName());
        Description description = new Description(request.description());
        Money basePrice = new Money(request.basePrice());
        Slug slug = new Slug(request.productName());

        if (productRepository.existsBySlug(slug) || productRepository.existsByProductName(productName)) {
            throw new ProductAlreadyExistsException();
        }

        if (request.variants() == null || request.variants().isEmpty()) {
            throw new EmptyProductVariantsException();
        }

        Category category = categoryRepository.findBySlug(new Slug(request.categorySlug()))
                .orElseThrow(CategoryNotFoundException::new);

        if (!category.isActive()) {
            throw new CategoryInactiveException();
        }

        Product product = Product.create(
                category,
                productName,
                description,
                basePrice
        );

        for (var variantRequest : request.variants()) {
            ProductVariant variant = ProductVariant.create(
                    new Color(variantRequest.color()),
                    new Size(variantRequest.size()),
                    new Money(variantRequest.price()),
                    new StockQuantity(variantRequest.stockQuantity())
            );
            product.addVariant(variant);
        }

        productRepository.save(product);

        return productMapper.toProductDetailsResponse(product);
    }
}