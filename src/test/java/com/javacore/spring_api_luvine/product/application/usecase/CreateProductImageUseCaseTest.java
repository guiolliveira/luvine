package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.application.dto.CreateImageRequest;
import com.javacore.spring_api_luvine.product.application.dto.ProductImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.UploadResult;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductImage;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.exception.ProductNotFoundException;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import com.javacore.spring_api_luvine.product.infrastructure.storage.StorageService;
import com.javacore.spring_api_luvine.product.infrastructure.storage.validation.FileImageValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@DisplayName("CreateProductImageUseCase")
@ExtendWith(MockitoExtension.class)
class CreateProductImageUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private FileImageValidator imageValidator;
    @Mock private StorageService storageService;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private CreateProductImageUseCase useCase;

    // --- HELPERS -------------------------------------------------------------

    private static final UUID PRODUCT_PUBLIC_ID = UUID.randomUUID();
    private static final UUID VARIANT_PUBLIC_ID = UUID.randomUUID();
    private static final String IMAGE_URL = "https://cdn.example.com/image.jpg";
    private static final String STORAGE_KEY = "products/abc/image.jpg";

    private CreateImageRequest validRequest() {
        return new CreateImageRequest("Camiseta branca frente", 1, false);
    }

    private MultipartFile mockFile() {
        return mock(MultipartFile.class);
    }

    private Product mockProductWithVariant(ProductVariant variant) {
        Product product = mock(Product.class);
        given(product.getPublicId()).willReturn(PRODUCT_PUBLIC_ID);
        given(product.findVariantByPublicId(VARIANT_PUBLIC_ID)).willReturn(variant);
        return product;
    }

    private ProductVariant mockVariant() {
        ProductVariant variant = mock(ProductVariant.class);
        given(variant.getPublicId()).willReturn(VARIANT_PUBLIC_ID);
        return variant;
    }

    // --- EXECUTE() -----------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve criar imagem com sucesso e retornar ProductImageResponse")
        void execute_validRequest_returnsProductImageResponse() {
            ProductVariant variant = mockVariant();
            Product product = mockProductWithVariant(variant);
            UploadResult uploadResult = new UploadResult(IMAGE_URL, STORAGE_KEY);
            ProductImageResponse expectedResponse = mock(ProductImageResponse.class);

            willDoNothing().given(imageValidator).validate(any());
            given(productRepository.findByPublicId(PRODUCT_PUBLIC_ID)).willReturn(Optional.of(product));
            given(storageService.upload(any(), anyString(), anyString())).willReturn(uploadResult);
            given(productMapper.toProductImageResponse(any(ProductImage.class))).willReturn(expectedResponse);

            ProductImageResponse response = useCase.execute(PRODUCT_PUBLIC_ID, VARIANT_PUBLIC_ID, mockFile(), validRequest());

            assertThat(response).isEqualTo(expectedResponse);
            then(productRepository).should().saveAndFlush(any(Product.class));
        }

        @Test
        @DisplayName("deve validar o arquivo antes de qualquer outra operação")
        void execute_validRequest_validatesFileFirst() {
            MultipartFile file = mockFile();
            willDoNothing().given(imageValidator).validate(file);
            given(productRepository.findByPublicId(PRODUCT_PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(ProductNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(PRODUCT_PUBLIC_ID, VARIANT_PUBLIC_ID, file, validRequest()));

            then(imageValidator).should().validate(file);
        }

        @Test
        @DisplayName("deve lançar ProductNotFoundException quando produto não existe")
        void execute_productNotFound_throwsProductNotFoundException() {
            willDoNothing().given(imageValidator).validate(any());
            given(productRepository.findByPublicId(PRODUCT_PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(ProductNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(PRODUCT_PUBLIC_ID, VARIANT_PUBLIC_ID, mockFile(), validRequest()));

            then(storageService).should(never()).upload(any(), any(), any());
        }

        @Test
        @DisplayName("deve fazer rollback do upload quando ocorre erro após o upload")
        void execute_errorAfterUpload_deletesUploadedFile() {
            ProductVariant variant = mockVariant();
            Product product = mockProductWithVariant(variant);
            UploadResult uploadResult = new UploadResult(IMAGE_URL, STORAGE_KEY);

            willDoNothing().given(imageValidator).validate(any());
            given(productRepository.findByPublicId(PRODUCT_PUBLIC_ID)).willReturn(Optional.of(product));
            given(storageService.upload(any(), anyString(), anyString())).willReturn(uploadResult);
            given(productRepository.saveAndFlush(any())).willThrow(new RuntimeException("DB error"));

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> useCase.execute(PRODUCT_PUBLIC_ID, VARIANT_PUBLIC_ID, mockFile(), validRequest()));

            then(storageService).should().delete(STORAGE_KEY);
        }

        @Test
        @DisplayName("não deve tentar deletar arquivo quando o upload falha")
        void execute_uploadFails_doesNotCallDelete() {
            ProductVariant variant = mockVariant();
            Product product = mockProductWithVariant(variant);

            willDoNothing().given(imageValidator).validate(any());
            given(productRepository.findByPublicId(PRODUCT_PUBLIC_ID)).willReturn(Optional.of(product));
            given(storageService.upload(any(), anyString(), anyString()))
                    .willThrow(new RuntimeException("Upload error"));

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> useCase.execute(PRODUCT_PUBLIC_ID, VARIANT_PUBLIC_ID, mockFile(), validRequest()));

            then(storageService).should(never()).delete(anyString());
        }

        @Test
        @DisplayName("deve propagar a exceção original após tentativa de rollback")
        void execute_errorAfterUpload_propagatesOriginalException() {
            ProductVariant variant = mockVariant();
            Product product = mockProductWithVariant(variant);
            UploadResult uploadResult = new UploadResult(IMAGE_URL, STORAGE_KEY);
            RuntimeException originalException = new RuntimeException("DB error");

            willDoNothing().given(imageValidator).validate(any());
            given(productRepository.findByPublicId(PRODUCT_PUBLIC_ID)).willReturn(Optional.of(product));
            given(storageService.upload(any(), anyString(), anyString())).willReturn(uploadResult);
            given(productRepository.saveAndFlush(any())).willThrow(originalException);

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> useCase.execute(PRODUCT_PUBLIC_ID, VARIANT_PUBLIC_ID, mockFile(), validRequest()))
                    .withMessage("DB error");
        }

        @Test
        @DisplayName("deve fazer upload na pasta correta incluindo publicId do produto e da variante")
        void execute_validRequest_uploadsToCorrectFolder() {
            ProductVariant variant = mockVariant();
            Product product = mockProductWithVariant(variant);
            UploadResult uploadResult = new UploadResult(IMAGE_URL, STORAGE_KEY);

            willDoNothing().given(imageValidator).validate(any());
            given(productRepository.findByPublicId(PRODUCT_PUBLIC_ID)).willReturn(Optional.of(product));
            given(storageService.upload(any(), anyString(), anyString())).willReturn(uploadResult);
            given(productMapper.toProductImageResponse(any())).willReturn(mock(ProductImageResponse.class));

            useCase.execute(PRODUCT_PUBLIC_ID, VARIANT_PUBLIC_ID, mockFile(), validRequest());

            String expectedFolder = String.format("products/%s/variants/%s/images",
                    PRODUCT_PUBLIC_ID, VARIANT_PUBLIC_ID);
            then(storageService).should().upload(any(), eq(expectedFolder), anyString());
        }
    }
}