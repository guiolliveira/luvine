package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.application.dto.CategoryDetailsResponse;
import com.javacore.spring_api_luvine.product.application.mapper.CategoryMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("GetAdminCategoryDetailsUseCase")
@ExtendWith(MockitoExtension.class)
class GetAdminCategoryDetailsUseCaseTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks
    private GetAdminCategoryDetailsUseCase getAdminCategoryDetailsUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID CATEGORY_ID = UUID.randomUUID();

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve retornar CategoryDetailsResponse quando categoria encontrada")
        void execute_categoryFound_returnsCategoryDetailsResponse() {
            Category category = mock(Category.class);
            CategoryDetailsResponse expected = mock(CategoryDetailsResponse.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(categoryMapper.toCategoryDetailsResponse(category)).willReturn(expected);

            CategoryDetailsResponse result = getAdminCategoryDetailsUseCase.execute(CATEGORY_ID);

            assertThat(result).isNotNull().isEqualTo(expected);
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não encontrada")
        void execute_categoryNotFound_throwsCategoryNotFoundException() {
            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(CategoryNotFoundException.class)
                    .isThrownBy(() -> getAdminCategoryDetailsUseCase.execute(CATEGORY_ID));
        }

        @Test
        @DisplayName("não deve chamar mapper quando categoria não encontrada")
        void execute_categoryNotFound_doesNotCallMapper() {
            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.empty());

            try { getAdminCategoryDetailsUseCase.execute(CATEGORY_ID); } catch (CategoryNotFoundException ignored) {}

            then(categoryMapper).should(never()).toCategoryDetailsResponse(any(Category.class));
        }

        @Test
        @DisplayName("deve mapear a categoria encontrada para CategoryDetailsResponse")
        void execute_categoryFound_mapsCorrectCategory() {
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(categoryMapper.toCategoryDetailsResponse(category)).willReturn(mock(CategoryDetailsResponse.class));

            getAdminCategoryDetailsUseCase.execute(CATEGORY_ID);

            then(categoryMapper).should().toCategoryDetailsResponse(category);
        }
    }
}