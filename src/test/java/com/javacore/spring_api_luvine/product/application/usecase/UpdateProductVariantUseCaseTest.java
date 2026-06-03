package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.application.dto.ProductVariantResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateVariantRequest;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.domain.exception.VariantAlreadyExistsException;
import com.javacore.spring_api_luvine.product.domain.exception.VariantNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.Color;
import com.javacore.spring_api_luvine.product.domain.valueObject.Money;
import com.javacore.spring_api_luvine.product.domain.valueObject.Size;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@DisplayName("UpdateProductVariantUseCase")
@ExtendWith(MockitoExtension.class)
class UpdateProductVariantUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private UpdateProductVariantUseCase updateProductVariantUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID VARIANT_ID = UUID.randomUUID();

    private UpdateVariantRequest requestWith(String color, String size, BigDecimal price) {
        return new UpdateVariantRequest(color, size, price);
    }

    private UpdateVariantRequest emptyRequest() {
        return requestWith(null, null, null);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve retornar ProductVariantResponse com sucesso")
        void execute_validRequest_returnsProductVariantResponse() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);
            ProductVariantResponse response = mock(ProductVariantResponse.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(productMapper.toProductVariantResponse(variant)).willReturn(response);

            ProductVariantResponse result = updateProductVariantUseCase.execute(
                    PRODUCT_ID, VARIANT_ID, emptyRequest());

            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException quando produto não encontrado")
        void execute_productNotFound_throwsProductNotFoundException() {
            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(ProductNotFoundException.class)
                    .isThrownBy(() -> updateProductVariantUseCase.execute(
                            PRODUCT_ID, VARIANT_ID, emptyRequest()));

            then(productMapper).should(never()).toProductVariantResponse(any());
        }

        @Test
        @DisplayName("deve lançar VariantNotFoundException quando variante não encontrada")
        void execute_variantNotFound_throwsVariantNotFoundException() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willThrow(new VariantNotFoundException());

            assertThatExceptionOfType(VariantNotFoundException.class)
                    .isThrownBy(() -> updateProductVariantUseCase.execute(
                            PRODUCT_ID, VARIANT_ID, emptyRequest()));
        }

        @Test
        @DisplayName("deve chamar updateVariantAttributes quando nova cor é fornecida")
        void execute_newColor_callsUpdateVariantAttributes() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.getSize()).willReturn(new Size("M"));
            given(product.updateVariantAttributes(any(), any(Color.class), any(Size.class))).willReturn(variant);
            given(productMapper.toProductVariantResponse(variant)).willReturn(mock(ProductVariantResponse.class));

            updateProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID, requestWith("Vermelho", null, null));

            then(product).should().updateVariantAttributes(VARIANT_ID, new Color("Vermelho"), new Size("M"));
        }

        @Test
        @DisplayName("deve chamar updateVariantAttributes quando novo tamanho é fornecido")
        void execute_newSize_callsUpdateVariantAttributes() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.getColor()).willReturn(new Color("Azul"));
            given(product.updateVariantAttributes(any(), any(Color.class), any(Size.class))).willReturn(variant);
            given(productMapper.toProductVariantResponse(variant)).willReturn(mock(ProductVariantResponse.class));

            updateProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID, requestWith(null, "G", null));

            then(product).should().updateVariantAttributes(VARIANT_ID, new Color("Azul"), new Size("G"));
        }

        @Test
        @DisplayName("deve lançar VariantAlreadyExistsException quando combinação cor+tamanho já existe")
        void execute_duplicateColorSize_throwsVariantAlreadyExistsException() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.getSize()).willReturn(new Size("M"));
            given(product.updateVariantAttributes(any(), any(), any()))
                    .willThrow(new VariantAlreadyExistsException());

            assertThatExceptionOfType(VariantAlreadyExistsException.class)
                    .isThrownBy(() -> updateProductVariantUseCase.execute(
                            PRODUCT_ID, VARIANT_ID, requestWith("Azul", null, null)));
        }

        @Test
        @DisplayName("deve alterar preço quando newPrice é fornecido")
        void execute_newPrice_changesPrice() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(productMapper.toProductVariantResponse(variant)).willReturn(mock(ProductVariantResponse.class));

            updateProductVariantUseCase.execute(
                    PRODUCT_ID, VARIANT_ID, requestWith(null, null, new BigDecimal("149.90")));

            then(variant).should().changePrice(new Money(new BigDecimal("149.90")));
        }

        @Test
        @DisplayName("deve propagar UnchangedValueException quando preço é igual ao atual")
        void execute_samePrice_propagatesUnchangedValueException() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            doThrow(new UnchangedValueException("A variante já possui o preço informado"))
                    .when(variant).changePrice(any(Money.class));

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> updateProductVariantUseCase.execute(
                            PRODUCT_ID, VARIANT_ID, requestWith(null, null, new BigDecimal("99.90"))));
        }

        @Test
        @DisplayName("não deve chamar updateVariantAttributes quando cor e tamanho são nulos")
        void execute_nullColorAndSize_doesNotCallUpdateAttributes() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(productMapper.toProductVariantResponse(variant)).willReturn(mock(ProductVariantResponse.class));

            updateProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID, emptyRequest());

            then(product).should(never()).updateVariantAttributes(any(), any(), any());
        }

        @Test
        @DisplayName("não deve alterar preço quando newPrice é nulo")
        void execute_nullPrice_doesNotChangePrice() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(productMapper.toProductVariantResponse(variant)).willReturn(mock(ProductVariantResponse.class));

            updateProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID, emptyRequest());

            then(variant).should(never()).changePrice(any());
        }
    }
}