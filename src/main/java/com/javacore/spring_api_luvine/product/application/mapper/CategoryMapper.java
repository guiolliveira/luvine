package com.javacore.spring_api_luvine.product.application.mapper;

import com.javacore.spring_api_luvine.product.application.dto.CategoryDetailsResponse;
import com.javacore.spring_api_luvine.product.application.dto.CategoryImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.CategorySummaryResponse;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.entity.CategoryImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "categoryName", source = "categoryName.value")
    @Mapping(target = "slug", source = "slug.value")
    @Mapping(target = "description", source = "description.value")
    CategoryDetailsResponse toCategoryDetailsResponse(Category category);

    @Mapping(target = "categoryName", source = "categoryName.value")
    @Mapping(target = "slug", source = "slug.value")
    CategorySummaryResponse toCategorySummaryResponse(Category category);

    @Mapping(target = "altText", source = "altText.value")
    CategoryImageResponse toCategoryImageResponse(CategoryImage categoryImage);
}