package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.application.dto.ProductVariantResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateVariantStockRequest;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.StockQuantity;
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

@DisplayName("DecreaseProductVariantUseCase")
@ExtendWith(MockitoExtension.class)
class DecreaseProductVariantUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private DecreaseProductVariantUseCase decreaseProductVariantUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID VARIANT_ID = UUID.randomUUID();
    private static final int STOCK_QUANTITY = 5;

    private UpdateVariantStockRequest validRequest() {
        return new UpdateVariantStockRequest(STOCK_QUANTITY);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve diminuir estoque e retornar ProductVariantResponse")
        void execute_productAndVariantFound_returnsProductVariantResponse() {
            ProductVariant variant = mock(ProductVariant.class);
            Product product = mock(Product.class);
            ProductVariantResponse expected = mock(ProductVariantResponse.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(productMapper.toProductVariantResponse(variant)).willReturn(expected);

            ProductVariantResponse result = decreaseProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID,
                    validRequest());

            assertThat(result).isNotNull().isEqualTo(expected);
        }

        @Test
        @DisplayName("deve chamar decreaseStock com StockQuantity correto")
        void execute_validRequest_callsDecreaseStockWithCorrectQuantity() {
            ProductVariant variant = mock(ProductVariant.class);
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(productMapper.toProductVariantResponse(variant)).willReturn(mock(ProductVariantResponse.class));

            decreaseProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID, validRequest());

            then(variant).should().decreaseStock(new StockQuantity(STOCK_QUANTITY));
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException quando produto não encontrado")
        void execute_productNotFound_throwsProductNotFoundException() {
            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(ProductNotFoundException.class)
                    .isThrownBy(() -> decreaseProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID, validRequest()));
        }

        @Test
        @DisplayName("não deve chamar decreaseStock quando produto não encontrado")
        void execute_productNotFound_doesNotCallDecreaseStock() {
            ProductVariant variant = mock(ProductVariant.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            try {
                decreaseProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID, validRequest());
            } catch (ProductNotFoundException ignored) {}

            then(variant).should(never()).decreaseStock(any(StockQuantity.class));
        }

        @Test
        @DisplayName("deve propagar exceção quando variante não encontrada no produto")
        void execute_variantNotFound_propagatesException() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willThrow(new RuntimeException("variant not found"));

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> decreaseProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID, validRequest()));
        }

        @Test
        @DisplayName("deve buscar a variante usando o variantPublicId correto")
        void execute_productFound_queriesVariantWithCorrectId() {
            ProductVariant variant = mock(ProductVariant.class);
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(productMapper.toProductVariantResponse(variant)).willReturn(mock(ProductVariantResponse.class));

            decreaseProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID, validRequest());

            then(product).should().findVariantByPublicId(VARIANT_ID);
        }

        @Test
        @DisplayName("não deve chamar mapper quando produto não encontrado")
        void execute_productNotFound_doesNotCallMapper() {
            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            try {
                decreaseProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID, validRequest());
            } catch (ProductNotFoundException ignored) {}

            then(productMapper).should(never()).toProductVariantResponse(any(ProductVariant.class));
        }
    }
}