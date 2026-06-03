package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.application.dto.ProductDetailsResponse;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
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

@DisplayName("GetAdminProductDetailsUseCase")
@ExtendWith(MockitoExtension.class)
class GetAdminProductDetailsUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private GetAdminProductDetailsUseCase getAdminProductDetailsUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PRODUCT_ID = UUID.randomUUID();

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve retornar ProductDetailsResponse quando produto encontrado")
        void execute_productFound_returnsProductDetailsResponse() {
            Product product = mock(Product.class);
            ProductDetailsResponse expected = mock(ProductDetailsResponse.class);

            given(productRepository.findDetailsByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(productMapper.toProductDetailsResponse(product)).willReturn(expected);

            ProductDetailsResponse result = getAdminProductDetailsUseCase.execute(PRODUCT_ID);

            assertThat(result).isNotNull().isEqualTo(expected);
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException quando produto não encontrado")
        void execute_productNotFound_throwsProductNotFoundException() {
            given(productRepository.findDetailsByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(ProductNotFoundException.class)
                    .isThrownBy(() -> getAdminProductDetailsUseCase.execute(PRODUCT_ID));
        }

        @Test
        @DisplayName("não deve chamar mapper quando produto não encontrado")
        void execute_productNotFound_doesNotCallMapper() {
            given(productRepository.findDetailsByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            try { getAdminProductDetailsUseCase.execute(PRODUCT_ID); } catch (ProductNotFoundException ignored) {}

            then(productMapper).should(never()).toProductDetailsResponse(any(Product.class));
        }

        @Test
        @DisplayName("deve usar findDetailsByPublicId e não findByPublicId")
        void execute_always_usesFindDetailsByPublicId() {
            given(productRepository.findDetailsByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            try { getAdminProductDetailsUseCase.execute(PRODUCT_ID); } catch (ProductNotFoundException ignored) {}

            then(productRepository).should().findDetailsByPublicId(PRODUCT_ID);
            then(productRepository).should(never()).findByPublicId(any());
        }

        @Test
        @DisplayName("deve mapear o produto encontrado para ProductDetailsResponse")
        void execute_productFound_mapsCorrectProduct() {
            Product product = mock(Product.class);

            given(productRepository.findDetailsByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(productMapper.toProductDetailsResponse(product)).willReturn(mock(ProductDetailsResponse.class));

            getAdminProductDetailsUseCase.execute(PRODUCT_ID);

            then(productMapper).should().toProductDetailsResponse(product);
        }
    }
}