package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.application.dto.CategoryDetailsResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateCategoryRequest;
import com.javacore.spring_api_luvine.product.application.mapper.CategoryMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryAlreadyExistsException;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@DisplayName("UpdateCategoryUseCase")
@ExtendWith(MockitoExtension.class)
class UpdateCategoryUseCaseTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks
    private UpdateCategoryUseCase updateCategoryUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final UUID PARENT_ID = UUID.randomUUID();

    private UpdateCategoryRequest requestWith(String name, String description, UUID parentId) {
        return new UpdateCategoryRequest(name, description, parentId);
    }

    private UpdateCategoryRequest emptyRequest() {
        return requestWith(null, null, null);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve atualizar categoria e retornar CategoryDetailsResponse")
        void execute_validRequest_returnsCategoryDetailsResponse() {
            Category category = mock(Category.class);
            CategoryDetailsResponse response = mock(CategoryDetailsResponse.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(categoryMapper.toCategoryDetailsResponse(category)).willReturn(response);

            CategoryDetailsResponse result = updateCategoryUseCase.execute(CATEGORY_ID, emptyRequest());

            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não encontrada")
        void execute_categoryNotFound_throwsCategoryNotFoundException() {
            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(CategoryNotFoundException.class)
                    .isThrownBy(() -> updateCategoryUseCase.execute(CATEGORY_ID, emptyRequest()));

            then(categoryMapper).should(never()).toCategoryDetailsResponse(any());
        }

        @Test
        @DisplayName("deve alterar parent quando newParentPublicId é fornecido")
        void execute_validParentId_changesParent() {
            Category category = mock(Category.class);
            Category parent = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(categoryRepository.findByPublicId(PARENT_ID)).willReturn(Optional.of(parent));
            given(categoryMapper.toCategoryDetailsResponse(category)).willReturn(mock(CategoryDetailsResponse.class));

            updateCategoryUseCase.execute(CATEGORY_ID, requestWith(null, null, PARENT_ID));

            then(category).should().changeParent(parent);
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando parent não encontrado")
        void execute_parentNotFound_throwsCategoryNotFoundException() {
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(categoryRepository.findByPublicId(PARENT_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(CategoryNotFoundException.class)
                    .isThrownBy(() -> updateCategoryUseCase.execute(CATEGORY_ID, requestWith(null, null, PARENT_ID)));
        }

        @Test
        @DisplayName("deve renomear categoria quando newCategoryName é fornecido e único")
        void execute_uniqueName_renamesCategory() {
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(categoryRepository.existsBySlugAndIdNot(any(), any())).willReturn(false);
            given(categoryRepository.existsByCategoryNameAndIdNot(any(), any())).willReturn(false);
            given(categoryMapper.toCategoryDetailsResponse(category)).willReturn(mock(CategoryDetailsResponse.class));

            updateCategoryUseCase.execute(CATEGORY_ID, requestWith("Calçados", null, null));

            then(category).should().rename(any());
        }

        @Test
        @DisplayName("deve lançar CategoryAlreadyExistsException quando slug já está em uso")
        void execute_duplicateSlug_throwsCategoryAlreadyExistsException() {
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(categoryRepository.existsBySlugAndIdNot(any(), any())).willReturn(true);

            assertThatExceptionOfType(CategoryAlreadyExistsException.class)
                    .isThrownBy(() -> updateCategoryUseCase.execute(
                            CATEGORY_ID, requestWith("Calçados", null, null)));
        }

        @Test
        @DisplayName("deve lançar CategoryAlreadyExistsException quando nome já está em uso")
        void execute_duplicateName_throwsCategoryAlreadyExistsException() {
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(categoryRepository.existsBySlugAndIdNot(any(), any())).willReturn(false);
            given(categoryRepository.existsByCategoryNameAndIdNot(any(), any())).willReturn(true);

            assertThatExceptionOfType(CategoryAlreadyExistsException.class)
                    .isThrownBy(() -> updateCategoryUseCase.execute(
                            CATEGORY_ID, requestWith("Calçados", null, null)));
        }

        @Test
        @DisplayName("deve alterar descrição quando newDescription é fornecida")
        void execute_validDescription_changesDescription() {
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(categoryMapper.toCategoryDetailsResponse(category)).willReturn(mock(CategoryDetailsResponse.class));

            updateCategoryUseCase.execute(CATEGORY_ID, requestWith(null, "Nova descrição válida", null));

            then(category).should().changeDescription(any());
        }

        @Test
        @DisplayName("deve propagar UnchangedValueException ao renomear com mesmo nome")
        void execute_sameName_propagatesUnchangedValueException() {
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(categoryRepository.existsBySlugAndIdNot(any(), any())).willReturn(false);
            given(categoryRepository.existsByCategoryNameAndIdNot(any(), any())).willReturn(false);
            doThrow(new UnchangedValueException("A categoria já possui o nome informado"))
                    .when(category).rename(any());

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> updateCategoryUseCase.execute(
                            CATEGORY_ID, requestWith("Roupas", null, null)));
        }

        @Test
        @DisplayName("não deve alterar parent quando newParentPublicId é nulo")
        void execute_nullParentId_doesNotChangeParent() {
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(categoryMapper.toCategoryDetailsResponse(category)).willReturn(mock(CategoryDetailsResponse.class));

            updateCategoryUseCase.execute(CATEGORY_ID, emptyRequest());

            then(category).should(never()).changeParent(any());
        }

        @Test
        @DisplayName("não deve verificar duplicidade quando newCategoryName é nulo")
        void execute_nullName_doesNotCheckDuplicates() {
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(categoryMapper.toCategoryDetailsResponse(category)).willReturn(mock(CategoryDetailsResponse.class));

            updateCategoryUseCase.execute(CATEGORY_ID, emptyRequest());

            then(categoryRepository).should(never()).existsBySlugAndIdNot(any(), any());
            then(categoryRepository).should(never()).existsByCategoryNameAndIdNot(any(), any());
        }
    }
}