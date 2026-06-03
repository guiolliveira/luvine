package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductImage;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import com.javacore.spring_api_luvine.product.infrastructure.storage.StorageService;
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
import static org.mockito.Mockito.never;

@DisplayName("RemoveProductImageUseCase")
@ExtendWith(MockitoExtension.class)
class RemoveProductImageUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private StorageService storageService;

    @InjectMocks
    private RemoveProductImageUseCase removeProductImageUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID VARIANT_ID = UUID.randomUUID();
    private static final UUID IMAGE_ID = UUID.randomUUID();
    private static final String STORAGE_KEY = "products/abc/variants/xyz/images/img1.jpg";

    private ProductImage mockImage() {
        ProductImage image = mock(ProductImage.class);
        given(image.getStorageKey()).willReturn(STORAGE_KEY);
        return image;
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve remover imagem do storage e da variante com sucesso")
        void execute_allFound_deletesFromStorageAndRemovesFromVariant() {
            ProductImage image = mockImage();
            ProductVariant variant = mock(ProductVariant.class);
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.findImageByPublicId(IMAGE_ID)).willReturn(image);

            removeProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, IMAGE_ID);

            then(storageService).should().delete(STORAGE_KEY);
            then(variant).should().removeImage(image);
        }

        @Test
        @DisplayName("deve deletar do storage antes de remover da variante")
        void execute_allFound_deletesFromStorageBeforeRemovingFromVariant() {
            ProductImage image = mockImage();
            ProductVariant variant = mock(ProductVariant.class);
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.findImageByPublicId(IMAGE_ID)).willReturn(image);

            var inOrder = inOrder(storageService, variant);

            removeProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, IMAGE_ID);

            inOrder.verify(storageService).delete(STORAGE_KEY);
            inOrder.verify(variant).removeImage(image);
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException quando produto não encontrado")
        void execute_productNotFound_throwsProductNotFoundException() {
            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(ProductNotFoundException.class)
                    .isThrownBy(() -> removeProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, IMAGE_ID));
        }

        @Test
        @DisplayName("não deve chamar storageService quando produto não encontrado")
        void execute_productNotFound_doesNotCallStorage() {
            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.empty());

            try { removeProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, IMAGE_ID); } catch (ProductNotFoundException ignored) {}

            then(storageService).should(never()).delete(any());
        }

        @Test
        @DisplayName("deve propagar exceção quando variante não encontrada no produto")
        void execute_variantNotFound_propagatesException() {
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willThrow(new RuntimeException("variant not found"));

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> removeProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, IMAGE_ID));

            then(storageService).should(never()).delete(any());
        }

        @Test
        @DisplayName("deve propagar exceção quando imagem não encontrada na variante")
        void execute_imageNotFound_propagatesException() {
            ProductVariant variant = mock(ProductVariant.class);
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.findImageByPublicId(IMAGE_ID)).willThrow(new RuntimeException("image not found"));

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> removeProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, IMAGE_ID));

            then(storageService).should(never()).delete(any());
        }

        @Test
        @DisplayName("não deve chamar removeImage quando storageService lança exceção")
        void execute_storageDeleteFails_doesNotCallRemoveImage() {
            ProductImage image = mockImage();
            ProductVariant variant = mock(ProductVariant.class);
            Product product = mock(Product.class);

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.findImageByPublicId(IMAGE_ID)).willReturn(image);
            willThrow(new RuntimeException("storage error")).given(storageService).delete(STORAGE_KEY);

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> removeProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, IMAGE_ID));

            then(variant).should(never()).removeImage(any());
        }

        @Test
        @DisplayName("deve propagar exceção do storageService sem swallow")
        void execute_storageDeleteFails_rethrowsException() {
            ProductImage image = mockImage();
            ProductVariant variant = mock(ProductVariant.class);
            Product product = mock(Product.class);
            RuntimeException storageError = new RuntimeException("storage error");

            given(productRepository.findByPublicId(PRODUCT_ID)).willReturn(Optional.of(product));
            given(product.findVariantByPublicId(VARIANT_ID)).willReturn(variant);
            given(variant.findImageByPublicId(IMAGE_ID)).willReturn(image);
            willThrow(storageError).given(storageService).delete(STORAGE_KEY);

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> removeProductImageUseCase.execute(PRODUCT_ID, VARIANT_ID, IMAGE_ID))
                    .isSameAs(storageError);
        }
    }
}