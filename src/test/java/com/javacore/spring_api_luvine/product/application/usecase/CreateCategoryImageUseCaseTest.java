package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.application.dto.CategoryImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.CreateCategoryImageRequest;
import com.javacore.spring_api_luvine.product.application.dto.UploadResult;
import com.javacore.spring_api_luvine.product.application.mapper.CategoryMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.entity.CategoryImage;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
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
import static org.mockito.Mockito.never;

@DisplayName("CreateCategoryImageUseCase")
@ExtendWith(MockitoExtension.class)
class CreateCategoryImageUseCaseTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private StorageService storageService;
    @Mock private FileImageValidator imageValidator;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks
    private CreateCategoryImageUseCase createCategoryImageUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final String ALT_TEXT = "Imagem da categoria";
    private static final String IMAGE_URL = "https://storage.example.com/image.jpg";
    private static final String STORAGE_KEY = "categories/abc/images/xyz";

    private MultipartFile mockFile() {
        return mock(MultipartFile.class);
    }

    private CreateCategoryImageRequest validRequest() {
        return new CreateCategoryImageRequest(ALT_TEXT);
    }

    private Category mockCategory() {
        Category category = mock(Category.class);
        given(category.getPublicId()).willReturn(CATEGORY_ID);
        given(category.getImage()).willReturn(null);
        return category;
    }

    private UploadResult uploadResult() {
        return new UploadResult(IMAGE_URL, STORAGE_KEY);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve criar imagem da categoria com sucesso e retornar CategoryImageResponse")
        void execute_validRequest_returnsCategoryImageResponse() {
            MultipartFile file = mockFile();
            Category category = mockCategory();
            CategoryImageResponse expected = mock(CategoryImageResponse.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(storageService.upload(any(), anyString(), anyString())).willReturn(uploadResult());
            given(categoryMapper.toCategoryImageResponse(any(CategoryImage.class))).willReturn(expected);

            CategoryImageResponse result = createCategoryImageUseCase.execute(CATEGORY_ID, file, validRequest());

            assertThat(result).isNotNull().isEqualTo(expected);
        }

        @Test
        @DisplayName("deve validar o arquivo antes de qualquer outra operação")
        void execute_validRequest_validatesFileFirst() {
            MultipartFile file = mockFile();

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.empty());

            try { createCategoryImageUseCase.execute(CATEGORY_ID, file, validRequest()); } catch (Exception ignored) {}

            then(imageValidator).should().validate(file);
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não encontrada")
        void execute_categoryNotFound_throwsCategoryNotFoundException() {
            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(CategoryNotFoundException.class)
                    .isThrownBy(() -> createCategoryImageUseCase.execute(CATEGORY_ID, mockFile(), validRequest()));

            then(storageService).should(never()).upload(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("deve salvar categoria após adicionar imagem")
        void execute_validRequest_savesAndFlushesCategory() {
            MultipartFile file = mockFile();
            Category category = mockCategory();

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(storageService.upload(any(), anyString(), anyString())).willReturn(uploadResult());
            given(categoryMapper.toCategoryImageResponse(any())).willReturn(mock(CategoryImageResponse.class));

            createCategoryImageUseCase.execute(CATEGORY_ID, file, validRequest());

            then(categoryRepository).should().saveAndFlush(category);
        }

        @Test
        @DisplayName("deve deletar imagem antiga no storage quando categoria já possuía imagem")
        void execute_categoryWithExistingImage_deletesOldImageFromStorage() {
            MultipartFile file = mockFile();
            Category category = mock(Category.class);
            CategoryImage oldImage = mock(CategoryImage.class);
            String oldKey = "categories/old/key";

            given(category.getPublicId()).willReturn(CATEGORY_ID);
            given(category.getImage()).willReturn(oldImage);
            given(oldImage.getStorageKey()).willReturn(oldKey);
            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(storageService.upload(any(), anyString(), anyString())).willReturn(uploadResult());
            given(categoryMapper.toCategoryImageResponse(any())).willReturn(mock(CategoryImageResponse.class));

            createCategoryImageUseCase.execute(CATEGORY_ID, file, validRequest());

            then(storageService).should().delete(oldKey);
        }

        @Test
        @DisplayName("não deve deletar imagem antiga quando categoria não possuía imagem")
        void execute_categoryWithoutExistingImage_doesNotDeleteFromStorage() {
            MultipartFile file = mockFile();
            Category category = mockCategory();

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(storageService.upload(any(), anyString(), anyString())).willReturn(uploadResult());
            given(categoryMapper.toCategoryImageResponse(any())).willReturn(mock(CategoryImageResponse.class));

            createCategoryImageUseCase.execute(CATEGORY_ID, file, validRequest());

            then(storageService).should(never()).delete(anyString());
        }

        @Test
        @DisplayName("deve deletar imagem do storage e relançar exceção quando upload falha")
        void execute_uploadSucceedsButSaveFails_rollsBackStorageUpload() {
            MultipartFile file = mockFile();
            Category category = mockCategory();

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(storageService.upload(any(), anyString(), anyString())).willReturn(uploadResult());
            given(categoryRepository.saveAndFlush(any())).willThrow(new RuntimeException("db error"));

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> createCategoryImageUseCase.execute(CATEGORY_ID, file, validRequest()));

            then(storageService).should().delete(STORAGE_KEY);
        }

        @Test
        @DisplayName("não deve tentar rollback no storage quando upload já falhou")
        void execute_uploadFails_doesNotCallDelete() {
            MultipartFile file = mockFile();
            Category category = mockCategory();

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(storageService.upload(any(), anyString(), anyString())).willThrow(new RuntimeException("upload error"));

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> createCategoryImageUseCase.execute(CATEGORY_ID, file, validRequest()));

            then(storageService).should(never()).delete(anyString());
        }
    }
}