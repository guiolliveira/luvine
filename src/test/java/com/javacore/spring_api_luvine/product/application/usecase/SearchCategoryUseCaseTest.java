package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.application.dto.CategorySummaryResponse;
import com.javacore.spring_api_luvine.product.application.dto.SearchCategoryRequest;
import com.javacore.spring_api_luvine.product.application.mapper.CategoryMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@DisplayName("SearchCategoryUseCase")
@ExtendWith(MockitoExtension.class)
class SearchCategoryUseCaseTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks
    private SearchCategoryUseCase searchCategoryUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private SearchCategoryRequest emptyRequest() {
        return new SearchCategoryRequest(null, null, null, null);
    }

    private Pageable defaultPageable() {
        return PageRequest.of(0, 10);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve retornar página de categorias mapeadas")
        void execute_validRequest_returnsMappedPage() {
            Category category = mock(Category.class);
            CategorySummaryResponse response = mock(CategorySummaryResponse.class);
            Page<Category> page = new PageImpl<>(List.of(category));

            given(categoryRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);
            given(categoryMapper.toCategorySummaryResponse(category)).willReturn(response);

            Page<CategorySummaryResponse> result = searchCategoryUseCase.execute(emptyRequest(), defaultPageable());

            assertThat(result.getContent()).containsExactly(response);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("deve retornar página vazia quando nenhuma categoria encontrada")
        void execute_noResults_returnsEmptyPage() {
            given(categoryRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(Page.empty());

            Page<CategorySummaryResponse> result = searchCategoryUseCase.execute(emptyRequest(), defaultPageable());

            assertThat(result.getContent()).isEmpty();
            then(categoryMapper).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("deve mapear cada categoria exatamente uma vez")
        void execute_multipleResults_mapsEachOnce() {
            Category c1 = mock(Category.class);
            Category c2 = mock(Category.class);
            Category c3 = mock(Category.class);
            Page<Category> page = new PageImpl<>(List.of(c1, c2, c3));

            given(categoryRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);
            given(categoryMapper.toCategorySummaryResponse(any())).willReturn(mock(CategorySummaryResponse.class));

            searchCategoryUseCase.execute(emptyRequest(), defaultPageable());

            then(categoryMapper).should(times(3)).toCategorySummaryResponse(any());
        }

        @Test
        @DisplayName("deve passar Specification e Pageable corretos ao repositório")
        void execute_validRequest_passesSpecificationAndPageable() {
            Pageable pageable = PageRequest.of(2, 5);
            given(categoryRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(Page.empty());

            searchCategoryUseCase.execute(emptyRequest(), pageable);

            then(categoryRepository).should().findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("deve respeitar paginação retornada pelo repositório")
        void execute_pagedResult_respectsRepositoryPagination() {
            Page<Category> page = new PageImpl<>(
                    List.of(mock(Category.class)),
                    PageRequest.of(0, 10),
                    25
            );

            given(categoryRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);
            given(categoryMapper.toCategorySummaryResponse(any())).willReturn(mock(CategorySummaryResponse.class));

            Page<CategorySummaryResponse> result = searchCategoryUseCase.execute(emptyRequest(), defaultPageable());

            assertThat(result.getTotalElements()).isEqualTo(25);
            assertThat(result.getTotalPages()).isEqualTo(3);
        }
    }
}