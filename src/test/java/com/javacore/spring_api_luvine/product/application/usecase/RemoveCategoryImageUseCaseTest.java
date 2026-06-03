package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.entity.CategoryImage;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("RemoveCategoryImageUseCase")
@ExtendWith(MockitoExtension.class)
class RemoveCategoryImageUseCaseTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private StorageService storageService;

    @InjectMocks
    private RemoveCategoryImageUseCase removeCategoryImageUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final UUID IMAGE_ID = UUID.randomUUID();
    private static final String STORAGE_KEY = "categories/abc/images/img1.jpg";

    private CategoryImage mockImage() {
        CategoryImage image = mock(CategoryImage.class);
        given(image.getStorageKey()).willReturn(STORAGE_KEY);
        return image;
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve remover imagem do storage e da categoria com sucesso")
        void execute_allFound_deletesFromStorageAndRemovesFromCategory() {
            CategoryImage image = mockImage();
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(category.findImageByPublicId(IMAGE_ID)).willReturn(image);

            removeCategoryImageUseCase.execute(CATEGORY_ID, IMAGE_ID);

            then(storageService).should().delete(STORAGE_KEY);
            then(category).should().removeCategoryImage(image);
        }

        @Test
        @DisplayName("deve deletar do storage antes de remover da categoria")
        void execute_allFound_deletesFromStorageBeforeRemovingFromCategory() {
            CategoryImage image = mockImage();
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(category.findImageByPublicId(IMAGE_ID)).willReturn(image);

            var inOrder = inOrder(storageService, category);

            removeCategoryImageUseCase.execute(CATEGORY_ID, IMAGE_ID);

            inOrder.verify(storageService).delete(STORAGE_KEY);
            inOrder.verify(category).removeCategoryImage(image);
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não encontrada")
        void execute_categoryNotFound_throwsCategoryNotFoundException() {
            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(CategoryNotFoundException.class)
                    .isThrownBy(() -> removeCategoryImageUseCase.execute(CATEGORY_ID, IMAGE_ID));
        }

        @Test
        @DisplayName("não deve chamar storageService quando categoria não encontrada")
        void execute_categoryNotFound_doesNotCallStorage() {
            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.empty());

            try { removeCategoryImageUseCase.execute(CATEGORY_ID, IMAGE_ID); } catch (CategoryNotFoundException ignored) {}

            then(storageService).should(never()).delete(any());
        }

        @Test
        @DisplayName("não deve chamar removeCategoryImage quando categoria não encontrada")
        void execute_categoryNotFound_doesNotCallRemoveCategoryImage() {
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.empty());

            try { removeCategoryImageUseCase.execute(CATEGORY_ID, IMAGE_ID); } catch (CategoryNotFoundException ignored) {}

            then(category).should(never()).removeCategoryImage(any());
        }

        @Test
        @DisplayName("deve propagar exceção quando imagem não encontrada na categoria")
        void execute_imageNotFound_propagatesException() {
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(category.findImageByPublicId(IMAGE_ID)).willThrow(new RuntimeException("image not found"));

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> removeCategoryImageUseCase.execute(CATEGORY_ID, IMAGE_ID));

            then(storageService).should(never()).delete(any());
        }

        @Test
        @DisplayName("não deve chamar removeCategoryImage quando storageService lança exceção")
        void execute_storageDeleteFails_doesNotCallRemoveCategoryImage() {
            CategoryImage image = mockImage();
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(category.findImageByPublicId(IMAGE_ID)).willReturn(image);
            willThrow(new RuntimeException("storage error")).given(storageService).delete(STORAGE_KEY);

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> removeCategoryImageUseCase.execute(CATEGORY_ID, IMAGE_ID));

            then(category).should(never()).removeCategoryImage(any());
        }

        @Test
        @DisplayName("deve propagar exatamente a exceção lançada pelo storageService")
        void execute_storageDeleteFails_rethrowsExactSameException() {
            CategoryImage image = mockImage();
            Category category = mock(Category.class);
            RuntimeException storageError = new RuntimeException("storage error");

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(category.findImageByPublicId(IMAGE_ID)).willReturn(image);
            willThrow(storageError).given(storageService).delete(STORAGE_KEY);

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> removeCategoryImageUseCase.execute(CATEGORY_ID, IMAGE_ID))
                    .isSameAs(storageError);
        }
    }
}