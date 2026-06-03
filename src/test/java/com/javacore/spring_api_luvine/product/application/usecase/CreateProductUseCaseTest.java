package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.application.dto.CreateProductRequest;
import com.javacore.spring_api_luvine.product.application.dto.CreateVariantRequest;
import com.javacore.spring_api_luvine.product.application.dto.ProductDetailsResponse;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryInactiveException;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.domain.exception.EmptyProductVariantsException;
import com.javacore.spring_api_luvine.product.domain.exception.ProductAlreadyExistsException;
import com.javacore.spring_api_luvine.product.domain.valueObject.*;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@DisplayName("CreateProductUseCase")
@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private CreateProductUseCase useCase;

    // --- HELPERS -------------------------------------------------------------

    private static final String CATEGORY_SLUG = "camisetas";
    private static final String PRODUCT_NAME = "Camiseta Básica";
    private static final String DESCRIPTION = "Camiseta de algodão premium";
    private static final BigDecimal BASE_PRICE = new BigDecimal("99.90");

    private CreateVariantRequest variantRequest() {
        return new CreateVariantRequest("Branco", "M", new BigDecimal("99.90"), 10);
    }

    private CreateProductRequest validRequest() {
        return new CreateProductRequest(CATEGORY_SLUG, PRODUCT_NAME, DESCRIPTION, BASE_PRICE, List.of(variantRequest()));
    }

    private Category activeCategory() {
        return Category.create(null, new CategoryName(CATEGORY_SLUG), new Description(DESCRIPTION));
    }

    private Category inactiveCategory() {
        Category category = Category.create(null, new CategoryName(CATEGORY_SLUG), new Description(DESCRIPTION));
        category.deactivate();
        return category;
    }

    // --- EXECUTE() -----------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve criar produto com sucesso e retornar ProductDetailsResponse")
        void execute_validRequest_returnsProductDetailsResponse() {
            ProductDetailsResponse expectedResponse = mock(ProductDetailsResponse.class);

            given(productRepository.existsBySlug(any())).willReturn(false);
            given(productRepository.existsByProductName(any())).willReturn(false);
            given(categoryRepository.findBySlug(any())).willReturn(Optional.of(activeCategory()));
            given(productMapper.toProductDetailsResponse(any())).willReturn(expectedResponse);

            ProductDetailsResponse response = useCase.execute(validRequest());

            assertThat(response).isEqualTo(expectedResponse);
            then(productRepository).should().save(any(Product.class));
        }

        @Test
        @DisplayName("deve lançar ProductAlreadyExistsException quando slug já existe")
        void execute_slugAlreadyExists_throwsProductAlreadyExistsException() {
            given(productRepository.existsBySlug(any())).willReturn(true);

            assertThatExceptionOfType(ProductAlreadyExistsException.class)
                    .isThrownBy(() -> useCase.execute(validRequest()));

            then(productRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ProductAlreadyExistsException quando nome já existe")
        void execute_nameAlreadyExists_throwsProductAlreadyExistsException() {
            given(productRepository.existsBySlug(any())).willReturn(false);
            given(productRepository.existsByProductName(any())).willReturn(true);

            assertThatExceptionOfType(ProductAlreadyExistsException.class)
                    .isThrownBy(() -> useCase.execute(validRequest()));

            then(productRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar EmptyProductVariantsException quando lista de variantes é nula")
        void execute_nullVariants_throwsEmptyProductVariantsException() {
            CreateProductRequest request = new CreateProductRequest(
                    CATEGORY_SLUG, PRODUCT_NAME, DESCRIPTION, BASE_PRICE, null);

            given(productRepository.existsBySlug(any())).willReturn(false);
            given(productRepository.existsByProductName(any())).willReturn(false);

            assertThatExceptionOfType(EmptyProductVariantsException.class)
                    .isThrownBy(() -> useCase.execute(request));

            then(productRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar EmptyProductVariantsException quando lista de variantes está vazia")
        void execute_emptyVariants_throwsEmptyProductVariantsException() {
            CreateProductRequest request = new CreateProductRequest(
                    CATEGORY_SLUG, PRODUCT_NAME, DESCRIPTION, BASE_PRICE, List.of());

            given(productRepository.existsBySlug(any())).willReturn(false);
            given(productRepository.existsByProductName(any())).willReturn(false);

            assertThatExceptionOfType(EmptyProductVariantsException.class)
                    .isThrownBy(() -> useCase.execute(request));

            then(productRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não existe")
        void execute_categoryNotFound_throwsCategoryNotFoundException() {
            given(productRepository.existsBySlug(any())).willReturn(false);
            given(productRepository.existsByProductName(any())).willReturn(false);
            given(categoryRepository.findBySlug(any())).willReturn(Optional.empty());

            assertThatExceptionOfType(CategoryNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(validRequest()));

            then(productRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar CategoryInactiveException quando categoria está inativa")
        void execute_inactiveCategory_throwsCategoryInactiveException() {
            given(productRepository.existsBySlug(any())).willReturn(false);
            given(productRepository.existsByProductName(any())).willReturn(false);
            given(categoryRepository.findBySlug(any())).willReturn(Optional.of(inactiveCategory()));

            assertThatExceptionOfType(CategoryInactiveException.class)
                    .isThrownBy(() -> useCase.execute(validRequest()));

            then(productRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve persistir produto com todas as variantes da request")
        void execute_multipleVariants_persistsAllVariants() {
            List<CreateVariantRequest> variants = List.of(
                    new CreateVariantRequest("Branco", "M", new BigDecimal("99.90"), 10),
                    new CreateVariantRequest("Preto", "G", new BigDecimal("109.90"), 5)
            );
            CreateProductRequest request = new CreateProductRequest(
                    CATEGORY_SLUG, PRODUCT_NAME, DESCRIPTION, BASE_PRICE, variants);

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

            given(productRepository.existsBySlug(any())).willReturn(false);
            given(productRepository.existsByProductName(any())).willReturn(false);
            given(categoryRepository.findBySlug(any())).willReturn(Optional.of(activeCategory()));
            given(productMapper.toProductDetailsResponse(any())).willReturn(mock(ProductDetailsResponse.class));

            useCase.execute(request);

            then(productRepository).should().save(captor.capture());
            assertThat(captor.getValue().getVariants()).hasSize(2);
        }

        @Test
        @DisplayName("deve buscar categoria pelo slug correto")
        void execute_validRequest_searchesCategoryByCorrectSlug() {
            given(productRepository.existsBySlug(any())).willReturn(false);
            given(productRepository.existsByProductName(any())).willReturn(false);
            given(categoryRepository.findBySlug(new Slug(CATEGORY_SLUG))).willReturn(Optional.of(activeCategory()));
            given(productMapper.toProductDetailsResponse(any())).willReturn(mock(ProductDetailsResponse.class));

            useCase.execute(validRequest());

            then(categoryRepository).should().findBySlug(new Slug(CATEGORY_SLUG));
        }
    }
}