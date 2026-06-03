package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.application.dto.ProductImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateImageAltTextRequest;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductImage;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ImageNotFoundException;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.domain.exception.VariantNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.AltText;
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

@DisplayName("UpdateProductImageAltTextUseCase")
@ExtendWith(MockitoExtension.class)
class UpdateProductImageAltTextUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private UpdateProductImageAltTextUseCase updateProductImageAltTextUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID VARIANT_ID = UUID.randomUUID();
    private static final UUID IMAGE_ID = UUID.randomUUID();
    private static final String NEW_ALT_TEXT = "Novo texto alternativo";

    private UpdateImageAltTextRequest validRequest() {
        return new UpdateImageAltTextRequest(NEW_ALT_TEXT);
    }

    private UpdateImageAltTextRequest nullRequest() {
        return new UpdateImageAltTextRequest(null);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve alterar altText e retornar ProductImageResponse")
        void execute_validRequest_returnsProductImageResponse() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);
            ProductImage image = mock(ProductImage.class);
            ProductImageResponse response = mock(ProductImageResponse.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.findImageByPublicId(IMAGE_ID)).willReturn(image);
            given(productMapper.toProductImageResponse(image)).willReturn(response);

            ProductImageResponse result = updateProductImageAltTextUseCase.execute(
                    PRODUCT_ID, VARIANT_ID, IMAGE_ID, validRequest());

            assertThat(result).isEqualTo(response);
            then(image).should().changeAltText(new AltText(NEW_ALT_TEXT));
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException quando produto não encontrado")
        void execute_productNotFound_throwsProductNotFoundException() {
            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(ProductNotFoundException.class)
                    .isThrownBy(() -> updateProductImageAltTextUseCase.execute(
                            PRODUCT_ID, VARIANT_ID, IMAGE_ID, validRequest()));

            then(productMapper).should(never()).toProductImageResponse(any());
        }

        @Test
        @DisplayName("deve lançar VariantNotFoundException quando variante não encontrada")
        void execute_variantNotFound_throwsVariantNotFoundException() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willThrow(new VariantNotFoundException());

            assertThatExceptionOfType(VariantNotFoundException.class)
                    .isThrownBy(() -> updateProductImageAltTextUseCase.execute(
                            PRODUCT_ID, VARIANT_ID, IMAGE_ID, validRequest()));
        }

        @Test
        @DisplayName("deve lançar ImageNotFoundException quando imagem não encontrada")
        void execute_imageNotFound_throwsImageNotFoundException() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.findImageByPublicId(IMAGE_ID)).willThrow(new ImageNotFoundException());

            assertThatExceptionOfType(ImageNotFoundException.class)
                    .isThrownBy(() -> updateProductImageAltTextUseCase.execute(
                            PRODUCT_ID, VARIANT_ID, IMAGE_ID, validRequest()));
        }

        @Test
        @DisplayName("não deve alterar altText quando newAltText é nulo")
        void execute_nullAltText_doesNotChangeAltText() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);
            ProductImage image = mock(ProductImage.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.findImageByPublicId(IMAGE_ID)).willReturn(image);
            given(productMapper.toProductImageResponse(image)).willReturn(mock(ProductImageResponse.class));

            updateProductImageAltTextUseCase.execute(PRODUCT_ID, VARIANT_ID, IMAGE_ID, nullRequest());

            then(image).should(never()).changeAltText(any());
        }

        @Test
        @DisplayName("deve propagar UnchangedValueException quando altText é igual ao atual")
        void execute_sameAltText_propagatesUnchangedValueException() {
            Product product = mock(Product.class);
            ProductVariant variant = mock(ProductVariant.class);
            ProductImage image = mock(ProductImage.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.findImageByPublicId(IMAGE_ID)).willReturn(image);
            doThrow(new UnchangedValueException("A imagem já possui esse texto alternativo"))
                    .when(image).changeAltText(any(AltText.class));

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> updateProductImageAltTextUseCase.execute(
                            PRODUCT_ID, VARIANT_ID, IMAGE_ID, validRequest()));
        }
    }
}