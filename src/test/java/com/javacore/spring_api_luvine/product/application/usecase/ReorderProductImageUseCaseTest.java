package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.application.dto.ProductImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.ReorderImageRequest;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductImage;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("ReorderProductImageUseCase")
@ExtendWith(MockitoExtension.class)
class ReorderProductImageUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private ReorderProductImageUseCase reorderProductImageUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID VARIANT_ID = UUID.randomUUID();

    private ReorderImageRequest validRequest() {
        return new ReorderImageRequest(List.of(UUID.randomUUID(), UUID.randomUUID()));
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve reordenar imagens e retornar lista de ProductImageResponse")
        void execute_allFound_returnsReorderedProductImageResponseList() {
            ProductImage image1 = mock(ProductImage.class);
            ProductImage image2 = mock(ProductImage.class);
            ProductVariant variant = mock(ProductVariant.class);
            Product product = mock(Product.class);
            ProductImageResponse response1 = mock(ProductImageResponse.class);
            ProductImageResponse response2 = mock(ProductImageResponse.class);
            ReorderImageRequest request = validRequest();

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.getImages()).willReturn(List.of(image1, image2));
            given(productMapper.toProductImageResponse(image1)).willReturn(response1);
            given(productMapper.toProductImageResponse(image2)).willReturn(response2);

            List<ProductImageResponse> result = reorderProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, request);

            assertThat(result).containsExactly(response1, response2);
        }

        @Test
        @DisplayName("deve chamar reorderImages com os imagePublicIds do request")
        void execute_allFound_callsReorderImagesWithCorrectIds() {
            ProductVariant variant = mock(ProductVariant.class);
            Product product = mock(Product.class);
            ReorderImageRequest request = validRequest();

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.getImages()).willReturn(List.of());

            reorderProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, request);

            then(variant).should().reorderImages(request.imagePublicIds());
        }

        @Test
        @DisplayName("deve mapear todas as imagens da variante após reordenar")
        void execute_allFound_mapsAllImagesAfterReorder() {
            ProductImage image1 = mock(ProductImage.class);
            ProductImage image2 = mock(ProductImage.class);
            ProductVariant variant = mock(ProductVariant.class);
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.getImages()).willReturn(List.of(image1, image2));
            given(productMapper.toProductImageResponse(any(ProductImage.class)))
                    .willReturn(mock(ProductImageResponse.class));

            reorderProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, validRequest());

            then(productMapper).should().toProductImageResponse(image1);
            then(productMapper).should().toProductImageResponse(image2);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando variante não possui imagens")
        void execute_variantWithNoImages_returnsEmptyList() {
            ProductVariant variant = mock(ProductVariant.class);
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.getImages()).willReturn(List.of());

            List<ProductImageResponse> result = reorderProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, validRequest());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException quando produto não encontrado")
        void execute_productNotFound_throwsProductNotFoundException() {
            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(ProductNotFoundException.class)
                    .isThrownBy(() -> reorderProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, validRequest()));
        }

        @Test
        @DisplayName("não deve chamar reorderImages quando produto não encontrado")
        void execute_productNotFound_doesNotCallReorderImages() {
            ProductVariant variant = mock(ProductVariant.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            try {
                reorderProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, validRequest());
            } catch (ProductNotFoundException ignored) {}

            then(variant).should(never()).reorderImages(any());
        }

        @Test
        @DisplayName("deve propagar exceção quando variante não encontrada no produto")
        void execute_variantNotFound_propagatesException() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willThrow(new RuntimeException("variant not found"));

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> reorderProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, validRequest()));
        }
    }
}