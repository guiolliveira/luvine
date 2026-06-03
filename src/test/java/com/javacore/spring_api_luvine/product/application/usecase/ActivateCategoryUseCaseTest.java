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
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("ActivateCategoryUseCase")
@ExtendWith(MockitoExtension.class)
class ActivateCategoryUseCaseTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private ActivateCategoryUseCase activateCategoryUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PUBLIC_ID = UUID.randomUUID();

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve ativar categoria quando encontrada")
        void execute_categoryFound_activatesCategory() {
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(category));

            activateCategoryUseCase.execute(PUBLIC_ID);

            then(category).should().activate();
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não encontrada")
        void execute_categoryNotFound_throwsCategoryNotFoundException() {
            given(categoryRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(CategoryNotFoundException.class)
                    .isThrownBy(() -> activateCategoryUseCase.execute(PUBLIC_ID));
        }

        @Test
        @DisplayName("não deve chamar activate quando categoria não encontrada")
        void execute_categoryNotFound_doesNotCallActivate() {
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.empty());

            try { activateCategoryUseCase.execute(PUBLIC_ID); } catch (CategoryNotFoundException ignored) {}

            then(category).should(never()).activate();
        }
    }
}