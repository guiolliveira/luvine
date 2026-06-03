package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@DisplayName("DeactivateCategoryUseCase")
@ExtendWith(MockitoExtension.class)
class DeactivateCategoryUseCaseTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private DeactivateCategoryUseCase useCase;

    // --- HELPERS -------------------------------------------------------------

    private static final UUID CATEGORY_PUBLIC_ID = UUID.randomUUID();

    // --- EXECUTE() -----------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve desativar categoria com sucesso sem lançar exceção")
        void execute_existingCategory_deactivatesWithoutException() {
            Category category = mock(Category.class);
            given(categoryRepository.findByPublicId(CATEGORY_PUBLIC_ID)).willReturn(Optional.of(category));

            assertThatNoException()
                    .isThrownBy(() -> useCase.execute(CATEGORY_PUBLIC_ID));

            then(category).should().deactivate();
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não existe")
        void execute_categoryNotFound_throwsCategoryNotFoundException() {
            given(categoryRepository.findByPublicId(CATEGORY_PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(CategoryNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(CATEGORY_PUBLIC_ID));
        }

        @Test
        @DisplayName("não deve chamar deactivate quando categoria não existe")
        void execute_categoryNotFound_doesNotCallDeactivate() {
            Category category = mock(Category.class);
            given(categoryRepository.findByPublicId(CATEGORY_PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(CategoryNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(CATEGORY_PUBLIC_ID));

            then(category).should(never()).deactivate();
        }

        @Test
        @DisplayName("deve buscar categoria pelo publicId correto")
        void execute_validId_searchesByCorrectPublicId() {
            Category category = mock(Category.class);
            given(categoryRepository.findByPublicId(CATEGORY_PUBLIC_ID)).willReturn(Optional.of(category));

            useCase.execute(CATEGORY_PUBLIC_ID);

            then(categoryRepository).should().findByPublicId(CATEGORY_PUBLIC_ID);
        }
    }
}