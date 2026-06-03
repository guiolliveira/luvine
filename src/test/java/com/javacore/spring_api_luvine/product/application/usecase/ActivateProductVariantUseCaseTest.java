package com.javacore.spring_api_luvine.product.application.usecase;

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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;

@DisplayName("ActivateProductVariantUseCase")
@ExtendWith(MockitoExtension.class)
class ActivateProductVariantUseCaseTest {

    @Mock private ProductRepository productRepository;

    @InjectMocks
    private ActivateProductVariantUseCase activateProductVariantUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID VARIANT_ID = UUID.randomUUID();

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve ativar variante quando produto e variante encontrados")
        void execute_productAndVariantFound_activatesVariant() {
            ProductVariant variant = mock(ProductVariant.class);
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);

            activateProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID);

            then(variant).should().activate();
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException quando produto não encontrado")
        void execute_productNotFound_throwsProductNotFoundException() {
            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(ProductNotFoundException.class)
                    .isThrownBy(() -> activateProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID));
        }

        @Test
        @DisplayName("deve propagar exceção quando variante não encontrada no produto")
        void execute_variantNotFound_propagatesException() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willThrow(new RuntimeException("variant not found"));

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> activateProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID));
        }

        @Test
        @DisplayName("deve buscar a variante usando o variantPublicId correto")
        void execute_productFound_queriesVariantWithCorrectId() {
            ProductVariant variant = mock(ProductVariant.class);
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);

            activateProductVariantUseCase.execute(PRODUCT_ID, VARIANT_ID);

            then(product).should().findVariantByPublicId(VARIANT_ID);
        }
    }
}