package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.application.dto.ProductDetailsResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateProductRequest;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.Status;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryInactiveException;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.domain.exception.ProductAlreadyExistsException;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.Slug;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@DisplayName("UpdateProductUseCase")
@ExtendWith(MockitoExtension.class)
class UpdateProductUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private UpdateProductUseCase updateProductUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PRODUCT_ID = UUID.randomUUID();

    private UpdateProductRequest requestWith(
            String categorySlug, String name, String description,
            BigDecimal price, Status status) {
        return new UpdateProductRequest(categorySlug, name, description, price, status);
    }

    private UpdateProductRequest minimalRequest() {
        return requestWith(null, null, null, null, Status.ACTIVE);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve atualizar produto e retornar ProductDetailsResponse")
        void execute_validRequest_returnsProductDetailsResponse() {
            Product product = mock(Product.class);
            ProductDetailsResponse response = mock(ProductDetailsResponse.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(productMapper.toProductDetailsResponse(product)).willReturn(response);

            ProductDetailsResponse result = updateProductUseCase.execute(PRODUCT_ID, minimalRequest());

            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException quando produto não encontrado")
        void execute_productNotFound_throwsProductNotFoundException() {
            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(ProductNotFoundException.class)
                    .isThrownBy(() -> updateProductUseCase.execute(PRODUCT_ID, minimalRequest()));

            then(productMapper).should(never()).toProductDetailsResponse(any());
        }

        @Test
        @DisplayName("deve alterar categoria quando newCategorySlug é fornecido e categoria está ativa")
        void execute_validCategorySlug_changesCategory() {
            Product product = mock(Product.class);
            Category category = mock(Category.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(categoryRepository.findBySlug(any(Slug.class))).willReturn(Optional.of(category));
            given(category.isActive()).willReturn(true);
            given(productMapper.toProductDetailsResponse(product)).willReturn(mock(ProductDetailsResponse.class));

            updateProductUseCase.execute(PRODUCT_ID, requestWith("roupas", null, null, null, Status.ACTIVE));

            then(product).should().changeCategory(category);
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não encontrada")
        void execute_categoryNotFound_throwsCategoryNotFoundException() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(categoryRepository.findBySlug(any(Slug.class))).willReturn(Optional.empty());

            assertThatExceptionOfType(CategoryNotFoundException.class)
                    .isThrownBy(() -> updateProductUseCase.execute(
                            PRODUCT_ID, requestWith("inexistente", null, null, null, Status.ACTIVE)));
        }

        @Test
        @DisplayName("deve lançar CategoryInactiveException quando categoria está inativa")
        void execute_inactiveCategory_throwsCategoryInactiveException() {
            Product product = mock(Product.class);
            Category category = mock(Category.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(categoryRepository.findBySlug(any(Slug.class))).willReturn(Optional.of(category));
            given(category.isActive()).willReturn(false);

            assertThatExceptionOfType(CategoryInactiveException.class)
                    .isThrownBy(() -> updateProductUseCase.execute(
                            PRODUCT_ID, requestWith("roupas", null, null, null, Status.ACTIVE)));
        }

        @Test
        @DisplayName("deve lançar ProductAlreadyExistsException quando nome já está em uso por outro produto")
        void execute_duplicateName_throwsProductAlreadyExistsException() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(productRepository.existsByProductNameAndIdNot(any(), any())).willReturn(true);

            assertThatExceptionOfType(ProductAlreadyExistsException.class)
                    .isThrownBy(() -> updateProductUseCase.execute(
                            PRODUCT_ID, requestWith(null, "Camiseta Premium", null, null, Status.ACTIVE)));
        }

        @Test
        @DisplayName("deve lançar ProductAlreadyExistsException quando slug já está em uso por outro produto")
        void execute_duplicateSlug_throwsProductAlreadyExistsException() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(productRepository.existsByProductNameAndIdNot(any(), any())).willReturn(false);
            given(productRepository.existsBySlugAndIdNot(any(), any())).willReturn(true);

            assertThatExceptionOfType(ProductAlreadyExistsException.class)
                    .isThrownBy(() -> updateProductUseCase.execute(
                            PRODUCT_ID, requestWith(null, "Camiseta Premium", null, null, Status.ACTIVE)));
        }

        @Test
        @DisplayName("deve propagar UnchangedValueException quando status é igual ao atual")
        void execute_sameStatus_propagatesUnchangedValueException() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));

            // CORREÇÃO: Removidas as linhas de given(product).willAnswer(...) que causavam o erro.
            // Para métodos void, usamos doThrow().when(mock).metodo()
            Mockito.doThrow(new UnchangedValueException("O produto já possui o status informado"))
                    .when(product).changeStatus(Status.ACTIVE);

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> updateProductUseCase.execute(PRODUCT_ID, minimalRequest()));
        }

        @Test
        @DisplayName("não deve alterar categoria quando newCategorySlug é nulo")
        void execute_nullCategorySlug_doesNotChangeCategory() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(productMapper.toProductDetailsResponse(product)).willReturn(mock(ProductDetailsResponse.class));

            updateProductUseCase.execute(PRODUCT_ID, minimalRequest());

            then(categoryRepository).shouldHaveNoInteractions();
            then(product).should(never()).changeCategory(any());
        }

        @Test
        @DisplayName("não deve alterar nome quando newProductName é nulo")
        void execute_nullProductName_doesNotRename() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(productMapper.toProductDetailsResponse(product)).willReturn(mock(ProductDetailsResponse.class));

            updateProductUseCase.execute(PRODUCT_ID, minimalRequest());

            then(product).should(never()).rename(any());
        }

        @Test
        @DisplayName("não deve alterar descrição quando newDescription é nula")
        void execute_nullDescription_doesNotChangeDescription() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(productMapper.toProductDetailsResponse(product)).willReturn(mock(ProductDetailsResponse.class));

            updateProductUseCase.execute(PRODUCT_ID, minimalRequest());

            then(product).should(never()).changeDescription(any());
        }

        @Test
        @DisplayName("não deve alterar preço quando newBasePrice é nulo")
        void execute_nullBasePrice_doesNotChangePrice() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(productMapper.toProductDetailsResponse(product)).willReturn(mock(ProductDetailsResponse.class));

            updateProductUseCase.execute(PRODUCT_ID, minimalRequest());

            then(product).should(never()).changeBasePrice(any());
        }
    }
}