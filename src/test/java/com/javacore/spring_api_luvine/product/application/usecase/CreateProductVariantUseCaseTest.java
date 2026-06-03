package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.application.dto.CreateVariantRequest;
import com.javacore.spring_api_luvine.product.application.dto.ProductVariantResponse;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@DisplayName("CreateProductVariantUseCase")
@ExtendWith(MockitoExtension.class)
class CreateProductVariantUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private CreateProductVariantUseCase useCase;

    // --- HELPERS -------------------------------------------------------------

    private static final UUID PRODUCT_PUBLIC_ID = UUID.randomUUID();

    private CreateVariantRequest validRequest() {
        return new CreateVariantRequest("Azul", "P", new BigDecimal("89.90"), 20);
    }

    // --- EXECUTE() -----------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve criar variante com sucesso e retornar ProductVariantResponse")
        void execute_validRequest_returnsProductVariantResponse() {
            Product product = mock(Product.class);
            ProductVariantResponse expectedResponse = mock(ProductVariantResponse.class);

            given(productRepository.findByPublicId(PRODUCT_PUBLIC_ID)).willReturn(Optional.of(product));
            given(productMapper.toProductVariantResponse(any(ProductVariant.class))).willReturn(expectedResponse);

            ProductVariantResponse response = useCase.execute(PRODUCT_PUBLIC_ID, validRequest());

            assertThat(response).isEqualTo(expectedResponse);
            then(product).should().addVariant(any(ProductVariant.class));
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException quando produto não existe")
        void execute_productNotFound_throwsProductNotFoundException() {
            given(productRepository.findByPublicId(PRODUCT_PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(ProductNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(PRODUCT_PUBLIC_ID, validRequest()));

            then(productMapper).should(never()).toProductVariantResponse(any());
        }

        @Test
        @DisplayName("deve adicionar variante ao produto encontrado")
        void execute_validRequest_addsVariantToProduct() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_PUBLIC_ID)).willReturn(Optional.of(product));
            given(productMapper.toProductVariantResponse(any())).willReturn(mock(ProductVariantResponse.class));

            useCase.execute(PRODUCT_PUBLIC_ID, validRequest());

            then(product).should().addVariant(any(ProductVariant.class));
        }

        @Test
        @DisplayName("deve buscar produto pelo publicId correto")
        void execute_validRequest_searchesProductByCorrectPublicId() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_PUBLIC_ID)).willReturn(Optional.of(product));
            given(productMapper.toProductVariantResponse(any())).willReturn(mock(ProductVariantResponse.class));

            useCase.execute(PRODUCT_PUBLIC_ID, validRequest());

            then(productRepository).should().findByPublicId(PRODUCT_PUBLIC_ID);
        }
    }
}