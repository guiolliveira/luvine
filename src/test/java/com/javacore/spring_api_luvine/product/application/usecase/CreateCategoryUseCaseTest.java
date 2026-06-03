package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.application.dto.CategoryDetailsResponse;
import com.javacore.spring_api_luvine.product.application.dto.CreateCategoryRequest;
import com.javacore.spring_api_luvine.product.application.mapper.CategoryMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryAlreadyExistsException;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.CategoryName;
import com.javacore.spring_api_luvine.product.domain.valueObject.Slug;
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

@DisplayName("CreateCategoryUseCase")
@ExtendWith(MockitoExtension.class)
class CreateCategoryUseCaseTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks
    private CreateCategoryUseCase createCategoryUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PARENT_ID = UUID.randomUUID();
    private static final String CATEGORY_NAME = "Camisas Polo";
    private static final String DESCRIPTION = "Categoria de camisas polo masculinas";

    private CreateCategoryRequest validRequest() {
        return new CreateCategoryRequest(null, CATEGORY_NAME, DESCRIPTION);
    }

    private CreateCategoryRequest requestWithParent() {
        return new CreateCategoryRequest(PARENT_ID, CATEGORY_NAME, DESCRIPTION);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve criar categoria sem parent e retornar CategoryDetailsResponse")
        void execute_validRequestWithoutParent_returnsCategoryDetailsResponse() {
            CategoryDetailsResponse expected = mock(CategoryDetailsResponse.class);

            given(categoryRepository.existsBySlug(any(Slug.class))).willReturn(false);
            given(categoryRepository.existsByCategoryName(any(CategoryName.class))).willReturn(false);
            given(categoryMapper.toCategoryDetailsResponse(any(Category.class))).willReturn(expected);

            CategoryDetailsResponse result = createCategoryUseCase.execute(validRequest());

            assertThat(result).isNotNull().isEqualTo(expected);
            then(categoryRepository).should().save(any(Category.class));
        }

        @Test
        @DisplayName("deve criar categoria com parent quando parentPublicId fornecido")
        void execute_validRequestWithParent_createsWithParent() {
            Category parent = mock(Category.class);
            CategoryDetailsResponse expected = mock(CategoryDetailsResponse.class);

            given(categoryRepository.existsBySlug(any(Slug.class))).willReturn(false);
            given(categoryRepository.existsByCategoryName(any(CategoryName.class))).willReturn(false);
            given(categoryRepository.findByPublicId(PARENT_ID)).willReturn(Optional.of(parent));
            given(categoryMapper.toCategoryDetailsResponse(any(Category.class))).willReturn(expected);

            CategoryDetailsResponse result = createCategoryUseCase.execute(requestWithParent());

            assertThat(result).isNotNull().isEqualTo(expected);
        }

        @Test
        @DisplayName("deve lançar CategoryAlreadyExistsException quando slug já existe")
        void execute_slugAlreadyExists_throwsCategoryAlreadyExistsException() {
            given(categoryRepository.existsBySlug(any(Slug.class))).willReturn(true);

            assertThatExceptionOfType(CategoryAlreadyExistsException.class)
                    .isThrownBy(() -> createCategoryUseCase.execute(validRequest()));

            then(categoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar CategoryAlreadyExistsException quando nome já existe")
        void execute_categoryNameAlreadyExists_throwsCategoryAlreadyExistsException() {
            given(categoryRepository.existsBySlug(any(Slug.class))).willReturn(false);
            given(categoryRepository.existsByCategoryName(any(CategoryName.class))).willReturn(true);

            assertThatExceptionOfType(CategoryAlreadyExistsException.class)
                    .isThrownBy(() -> createCategoryUseCase.execute(validRequest()));

            then(categoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando parent não encontrado")
        void execute_parentNotFound_throwsCategoryNotFoundException() {
            given(categoryRepository.existsBySlug(any(Slug.class))).willReturn(false);
            given(categoryRepository.existsByCategoryName(any(CategoryName.class))).willReturn(false);
            given(categoryRepository.findByPublicId(PARENT_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(CategoryNotFoundException.class)
                    .isThrownBy(() -> createCategoryUseCase.execute(requestWithParent()));

            then(categoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("não deve buscar parent quando parentPublicId é nulo")
        void execute_nullParentId_doesNotQueryParent() {
            given(categoryRepository.existsBySlug(any(Slug.class))).willReturn(false);
            given(categoryRepository.existsByCategoryName(any(CategoryName.class))).willReturn(false);
            given(categoryMapper.toCategoryDetailsResponse(any())).willReturn(mock(CategoryDetailsResponse.class));

            createCategoryUseCase.execute(validRequest());

            then(categoryRepository).should(never()).findByPublicId(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando categoryName é inválido")
        void execute_invalidCategoryName_throwsException() {
            CreateCategoryRequest request = new CreateCategoryRequest(null, "a", DESCRIPTION);

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> createCategoryUseCase.execute(request));

            then(categoryRepository).should(never()).save(any());
        }
    }
}