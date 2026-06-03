package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.application.dto.ProductImageResponse;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductImage;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ImageAlreadyPrimaryException;
import com.javacore.spring_api_luvine.product.domain.exception.ImageNotFoundException;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.domain.exception.VariantNotFoundException;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@DisplayName("SetPrimaryProductImageUseCase")
@ExtendWith(MockitoExtension.class)
class SetPrimaryProductImageUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private SetPrimaryProductImageUseCase setPrimaryProductImageUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID VARIANT_ID = UUID.randomUUID();
    private static final UUID IMAGE_ID = UUID.randomUUID();

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve definir imagem como primária e retornar ProductImageResponse")
        void execute_validIds_returnsProductImageResponse() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);
            ProductImage image = mock(ProductImage.class);
            ProductImageResponse response = mock(ProductImageResponse.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.findImageByPublicId(IMAGE_ID)).willReturn(image);
            given(productMapper.toProductImageResponse(image)).willReturn(response);

            ProductImageResponse result = setPrimaryProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, IMAGE_ID);

            assertThat(result).isEqualTo(response);
            then(variant).should().setPrimary(image);
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException quando produto não encontrado")
        void execute_productNotFound_throwsProductNotFoundException() {
            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(ProductNotFoundException.class)
                    .isThrownBy(() -> setPrimaryProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, IMAGE_ID));

            then(productMapper).should(never()).toProductImageResponse(any());
        }

        @Test
        @DisplayName("deve lançar VariantNotFoundException quando variante não encontrada")
        void execute_variantNotFound_throwsVariantNotFoundException() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willThrow(new VariantNotFoundException());

            assertThatExceptionOfType(VariantNotFoundException.class)
                    .isThrownBy(() -> setPrimaryProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, IMAGE_ID));

            then(productMapper).should(never()).toProductImageResponse(any());
        }

        @Test
        @DisplayName("deve lançar ImageNotFoundException quando imagem não encontrada na variante")
        void execute_imageNotFound_throwsImageNotFoundException() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.findImageByPublicId(IMAGE_ID)).willThrow(new ImageNotFoundException());

            assertThatExceptionOfType(ImageNotFoundException.class)
                    .isThrownBy(() -> setPrimaryProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, IMAGE_ID));

            then(variant).should(never()).setPrimary(any());
        }

        @Test
        @DisplayName("deve propagar ImageAlreadyPrimaryException quando imagem já é primária")
        void execute_imageAlreadyPrimary_propagatesException() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);
            ProductImage image = mock(ProductImage.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.findImageByPublicId(IMAGE_ID)).willReturn(image);
            doThrow(new ImageAlreadyPrimaryException()).when(variant).setPrimary(image);

            assertThatExceptionOfType(ImageAlreadyPrimaryException.class)
                    .isThrownBy(() -> setPrimaryProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, IMAGE_ID));
        }
    }
}