package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.application.dto.ProductDetailsResponse;
import com.javacore.spring_api_luvine.product.application.dto.ProductDetailsResult;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.Status;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.Slug;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("GetProductDetailsUseCase")
@ExtendWith(MockitoExtension.class)
class GetProductDetailsUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private GetProductDetailsUseCase getProductDetailsUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final String SLUG_VALUE = "camiseta-premium";

    private Product mockProduct() {
        Product product = mock(Product.class);
        given(product.getSlug()).willReturn(new Slug(SLUG_VALUE));
        return product;
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve retornar ProductDetailsResult quando produto ativo encontrado")
        void execute_activeProductFound_returnsProductDetailsResult() {
            Product product = mockProduct();
            ProductDetailsResponse response = mock(ProductDetailsResponse.class);

            given(productRepository.findDetailsByPublicIdAndStatus(PRODUCT_ID, Status.ACTIVE))
                    .willReturn(Optional.of(product));
            given(productMapper.toProductDetailsResponse(product)).willReturn(response);

            ProductDetailsResult result = getProductDetailsUseCase.execute(PRODUCT_ID);

            assertThat(result).isNotNull();
            assertThat(result.response()).isEqualTo(response);
        }

        @Test
        @DisplayName("deve incluir o slug do produto no ProductDetailsResult")
        void execute_activeProductFound_includesSlugInResult() {
            Product product = mockProduct();

            given(productRepository.findDetailsByPublicIdAndStatus(PRODUCT_ID, Status.ACTIVE))
                    .willReturn(Optional.of(product));
            given(productMapper.toProductDetailsResponse(product)).willReturn(mock(ProductDetailsResponse.class));

            ProductDetailsResult result = getProductDetailsUseCase.execute(PRODUCT_ID);

            assertThat(result.currentSlug()).isEqualTo(SLUG_VALUE);
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException quando produto não encontrado ou inativo")
        void execute_productNotFoundOrInactive_throwsProductNotFoundException() {
            given(productRepository.findDetailsByPublicIdAndStatus(PRODUCT_ID, Status.ACTIVE))
                    .willReturn(Optional.empty());

            assertThatExceptionOfType(ProductNotFoundException.class)
                    .isThrownBy(() -> getProductDetailsUseCase.execute(PRODUCT_ID));
        }

        @Test
        @DisplayName("não deve chamar mapper quando produto não encontrado")
        void execute_productNotFound_doesNotCallMapper() {
            given(productRepository.findDetailsByPublicIdAndStatus(PRODUCT_ID, Status.ACTIVE))
                    .willReturn(Optional.empty());

            try { getProductDetailsUseCase.execute(PRODUCT_ID); } catch (ProductNotFoundException ignored) {}

            then(productMapper).should(never()).toProductDetailsResponse(any(Product.class));
        }

        @Test
        @DisplayName("deve usar findDetailsByPublicIdAndStatus com Status.ACTIVE")
        void execute_always_usesFindDetailsByPublicIdAndStatusActive() {
            given(productRepository.findDetailsByPublicIdAndStatus(PRODUCT_ID, Status.ACTIVE))
                    .willReturn(Optional.empty());

            try { getProductDetailsUseCase.execute(PRODUCT_ID); } catch (ProductNotFoundException ignored) {}

            then(productRepository).should().findDetailsByPublicIdAndStatus(PRODUCT_ID, Status.ACTIVE);
            then(productRepository).should(never()).findByPublicId(any());
        }

        @Test
        @DisplayName("deve mapear o produto encontrado para ProductDetailsResponse")
        void execute_productFound_mapsCorrectProduct() {
            Product product = mockProduct();

            given(productRepository.findDetailsByPublicIdAndStatus(PRODUCT_ID, Status.ACTIVE))
                    .willReturn(Optional.of(product));
            given(productMapper.toProductDetailsResponse(product)).willReturn(mock(ProductDetailsResponse.class));

            getProductDetailsUseCase.execute(PRODUCT_ID);

            then(productMapper).should().toProductDetailsResponse(product);
        }
    }
}