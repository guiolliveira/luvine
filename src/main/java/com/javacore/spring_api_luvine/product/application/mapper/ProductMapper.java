package com.javacore.spring_api_luvine.product.application.mapper;

import com.javacore.spring_api_luvine.product.application.dto.*;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductImage;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "productName", source = "productName.value")
    @Mapping(target = "description", source = "description.value")
    @Mapping(target = "slug", source = "slug.value")
    ProductDetailsResponse toProductDetailsResponse(Product product);

    ProductImageResponse toProductImageResponse(ProductImage productImage);

    @Mapping(target = "productName", source = "productName.value")
    @Mapping(target = "slug", source = "slug.value")
    @Mapping(target = "lowestPrice", source = "basePrice.value")
    ProductSummaryResponse toProductSummaryResponse(Product product);

    @Mapping(target = "sku", source = "sku.value")
    @Mapping(target = "color", source = "color.value")
    @Mapping(target = "size", source = "size.value")
    @Mapping(target = "price", source = "price.value")
    @Mapping(target = "stockQuantity", source = "stockQuantity.value")
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);
}