package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.application.dto.CategorySummaryResponse;
import com.javacore.spring_api_luvine.product.application.dto.SearchCategoryRequest;
import com.javacore.spring_api_luvine.product.application.mapper.CategoryMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import com.javacore.spring_api_luvine.product.infrastructure.specification.CategorySearchSpecifications;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("SearchAdminCategoryUseCase")
@ExtendWith(MockitoExtension.class)
class SearchAdminCategoryUseCaseTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks
    private SearchAdminCategoryUseCase searchAdminCategoryUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final Pageable PAGEABLE = PageRequest.of(0, 10);

    private SearchCategoryRequest validRequest() {
        return new SearchCategoryRequest(null, null, null, null);
    }

    private SearchCategoryRequest requestWithFilters() {
        return new SearchCategoryRequest("Camisas", true, null, null);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve retornar Page de CategorySummaryResponse com os resultados mapeados")
        void execute_validRequest_returnsMappedPage() {
            Category category = mock(Category.class);
            CategorySummaryResponse response = mock(CategorySummaryResponse.class);
            Page<Category> categoryPage = new PageImpl<>(List.of(category), PAGEABLE, 1);

            try (MockedStatic<CategorySearchSpecifications> specs = mockStatic(CategorySearchSpecifications.class)) {
                Specification<Category> spec = mock(Specification.class);
                specs.when(() -> CategorySearchSpecifications.build(any())).thenReturn(spec);

                given(categoryRepository.findAll(spec, PAGEABLE)).willReturn(categoryPage);
                given(categoryMapper.toCategorySummaryResponse(category)).willReturn(response);

                Page<CategorySummaryResponse> result = searchAdminCategoryUseCase.execute(validRequest(), PAGEABLE);

                assertThat(result).isNotNull();
                assertThat(result.getContent()).containsExactly(response);
            }
        }

        @Test
        @DisplayName("deve retornar Page vazia quando nenhuma categoria encontrada")
        void execute_noResults_returnsEmptyPage() {
            Page<Category> emptyPage = new PageImpl<>(List.of(), PAGEABLE, 0);

            try (MockedStatic<CategorySearchSpecifications> specs = mockStatic(CategorySearchSpecifications.class)) {
                Specification<Category> spec = mock(Specification.class);
                specs.when(() -> CategorySearchSpecifications.build(any())).thenReturn(spec);

                given(categoryRepository.findAll(spec, PAGEABLE)).willReturn(emptyPage);

                Page<CategorySummaryResponse> result = searchAdminCategoryUseCase.execute(validRequest(), PAGEABLE);

                assertThat(result).isNotNull();
                assertThat(result.getContent()).isEmpty();
                assertThat(result.getTotalElements()).isZero();
            }
        }

        @Test
        @DisplayName("deve construir specification a partir do request")
        void execute_validRequest_buildsSpecificationFromRequest() {
            SearchCategoryRequest request = requestWithFilters();
            Page<Category> emptyPage = new PageImpl<>(List.of(), PAGEABLE, 0);

            try (MockedStatic<CategorySearchSpecifications> specs = mockStatic(CategorySearchSpecifications.class)) {
                Specification<Category> spec = mock(Specification.class);
                specs.when(() -> CategorySearchSpecifications.build(request)).thenReturn(spec);

                given(categoryRepository.findAll(spec, PAGEABLE)).willReturn(emptyPage);

                searchAdminCategoryUseCase.execute(request, PAGEABLE);

                specs.verify(() -> CategorySearchSpecifications.build(request));
            }
        }

        @Test
        @DisplayName("deve mapear cada categoria retornada pelo repositório")
        void execute_multipleResults_mapsEachCategory() {
            Category category1 = mock(Category.class);
            Category category2 = mock(Category.class);
            Page<Category> categoryPage = new PageImpl<>(List.of(category1, category2), PAGEABLE, 2);

            try (MockedStatic<CategorySearchSpecifications> specs = mockStatic(CategorySearchSpecifications.class)) {
                Specification<Category> spec = mock(Specification.class);
                specs.when(() -> CategorySearchSpecifications.build(any())).thenReturn(spec);

                given(categoryRepository.findAll(spec, PAGEABLE)).willReturn(categoryPage);
                given(categoryMapper.toCategorySummaryResponse(any(Category.class)))
                        .willReturn(mock(CategorySummaryResponse.class));

                searchAdminCategoryUseCase.execute(validRequest(), PAGEABLE);

                then(categoryMapper).should().toCategorySummaryResponse(category1);
                then(categoryMapper).should().toCategorySummaryResponse(category2);
            }
        }

        @Test
        @DisplayName("deve preservar metadados de paginação no resultado")
        void execute_validRequest_preservesPaginationMetadata() {
            Pageable pageable = PageRequest.of(2, 5);
            Page<Category> categoryPage = new PageImpl<>(List.of(), pageable, 30);

            try (MockedStatic<CategorySearchSpecifications> specs = mockStatic(CategorySearchSpecifications.class)) {
                Specification<Category> spec = mock(Specification.class);
                specs.when(() -> CategorySearchSpecifications.build(any())).thenReturn(spec);

                given(categoryRepository.findAll(spec, pageable)).willReturn(categoryPage);

                Page<CategorySummaryResponse> result = searchAdminCategoryUseCase.execute(validRequest(), pageable);

                assertThat(result.getTotalElements()).isEqualTo(30);
                assertThat(result.getNumber()).isEqualTo(2);
                assertThat(result.getSize()).isEqualTo(5);
            }
        }
    }
}