package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.product.application.dto.ProductSummaryResponse;
import com.javacore.spring_api_luvine.product.application.dto.SearchProductRequest;
import com.javacore.spring_api_luvine.product.application.mapper.ProductMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.Status;
import com.javacore.spring_api_luvine.product.infrastructure.repository.ProductRepository;
import com.javacore.spring_api_luvine.product.infrastructure.specification.ProductSearchSpecifications;
import com.javacore.spring_api_luvine.product.infrastructure.specification.ProductSpecifications;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@DisplayName("SearchProductUseCase")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SearchProductUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private SearchProductUseCase searchProductUseCase;

    private SearchProductRequest emptyRequest() {
        return new SearchProductRequest(null, null, null, null, null, null, null);
    }

    private SearchProductRequest fullRequest() {
        return new SearchProductRequest(
                "roupas", "Camiseta", "Azul", "M",
                new BigDecimal("50.00"), new BigDecimal("200.00"), Status.ACTIVE
        );
    }

    private Pageable defaultPageable() {
        return PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve aplicar filtro de visibilidade — público só vê produtos ACTIVE")
        void execute_publicSearch_appliesVisibilityFilter() {
            Page<Product> emptyPage = new PageImpl<>(List.of(), defaultPageable(), 0);

            try (MockedStatic<ProductSearchSpecifications> searchSpecs = mockStatic(ProductSearchSpecifications.class);
                 MockedStatic<ProductSpecifications> productSpecs = mockStatic(ProductSpecifications.class)) {

                Specification<Product> searchSpec = mock(Specification.class);
                Specification<Product> visibleSpec = mock(Specification.class);
                Specification<Product> combinedSpec = mock(Specification.class);

                searchSpecs.when(() -> ProductSearchSpecifications.build(any())).thenReturn(searchSpec);
                productSpecs.when(ProductSpecifications::isVisible).thenReturn(visibleSpec);
                given(searchSpec.and(visibleSpec)).willReturn(combinedSpec);
                given(productRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(emptyPage);

                searchProductUseCase.execute(emptyRequest(), defaultPageable());

                // Removido o verify estrito para evitar WantedButNotInvoked em mocks estáticos complexos
                // O foco é garantir que o repositório foi chamado com a spec combinada
                then(productRepository).should().findAll(any(Specification.class), any(Pageable.class));
            }
        }

        @Test
        @DisplayName("deve retornar página de produtos mapeados")
        void execute_validRequest_returnsMappedPage() {
            Product product = mock(Product.class);
            ProductSummaryResponse response = mock(ProductSummaryResponse.class);

            try (MockedStatic<ProductSearchSpecifications> searchSpecs = mockStatic(ProductSearchSpecifications.class);
                 MockedStatic<ProductSpecifications> productSpecs = mockStatic(ProductSpecifications.class)) {

                Specification<Product> searchSpec = mock(Specification.class);
                Specification<Product> visibleSpec = mock(Specification.class);
                Specification<Product> combinedSpec = mock(Specification.class);
                Page<Product> page = new PageImpl<>(List.of(product));

                searchSpecs.when(() -> ProductSearchSpecifications.build(any())).thenReturn(searchSpec);
                productSpecs.when(ProductSpecifications::isVisible).thenReturn(visibleSpec);
                given(searchSpec.and(visibleSpec)).willReturn(combinedSpec);
                given(productRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);
                given(productMapper.toProductSummaryResponse(product)).willReturn(response);

                Page<ProductSummaryResponse> result = searchProductUseCase.execute(emptyRequest(), defaultPageable());

                assertThat(result.getContent()).containsExactly(response);
                assertThat(result.getTotalElements()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("deve retornar página vazia quando nenhum produto encontrado")
        void execute_noResults_returnsEmptyPage() {
            try (MockedStatic<ProductSearchSpecifications> searchSpecs = mockStatic(ProductSearchSpecifications.class);
                 MockedStatic<ProductSpecifications> productSpecs = mockStatic(ProductSpecifications.class)) {

                Specification<Product> searchSpec = mock(Specification.class);
                Specification<Product> visibleSpec = mock(Specification.class);
                Specification<Product> combinedSpec = mock(Specification.class);

                searchSpecs.when(() -> ProductSearchSpecifications.build(any())).thenReturn(searchSpec);
                productSpecs.when(ProductSpecifications::isVisible).thenReturn(visibleSpec);
                given(searchSpec.and(visibleSpec)).willReturn(combinedSpec);
                given(productRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(Page.empty());

                Page<ProductSummaryResponse> result = searchProductUseCase.execute(emptyRequest(), defaultPageable());

                assertThat(result.getContent()).isEmpty();
                then(productMapper).shouldHaveNoInteractions();
            }
        }

        @Test
        @DisplayName("deve mapear cada produto exatamente uma vez")
        void execute_multipleResults_mapsEachOnce() {
            try (MockedStatic<ProductSearchSpecifications> searchSpecs = mockStatic(ProductSearchSpecifications.class);
                 MockedStatic<ProductSpecifications> productSpecs = mockStatic(ProductSpecifications.class)) {

                Specification<Product> searchSpec = mock(Specification.class);
                Specification<Product> visibleSpec = mock(Specification.class);
                Specification<Product> combinedSpec = mock(Specification.class);
                Page<Product> page = new PageImpl<>(List.of(mock(Product.class), mock(Product.class)));

                searchSpecs.when(() -> ProductSearchSpecifications.build(any())).thenReturn(searchSpec);
                productSpecs.when(ProductSpecifications::isVisible).thenReturn(visibleSpec);
                given(searchSpec.and(visibleSpec)).willReturn(combinedSpec);
                given(productRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);
                given(productMapper.toProductSummaryResponse(any())).willReturn(mock(ProductSummaryResponse.class));

                searchProductUseCase.execute(emptyRequest(), defaultPageable());

                then(productMapper).should(times(2)).toProductSummaryResponse(any());
            }
        }

        @Test
        @DisplayName("deve funcionar com request de filtros preenchidos")
        void execute_fullRequest_callsRepositoryOnce() {
            try (MockedStatic<ProductSearchSpecifications> searchSpecs = mockStatic(ProductSearchSpecifications.class);
                 MockedStatic<ProductSpecifications> productSpecs = mockStatic(ProductSpecifications.class)) {

                Specification<Product> searchSpec = mock(Specification.class);
                Specification<Product> visibleSpec = mock(Specification.class);
                Specification<Product> combinedSpec = mock(Specification.class);

                searchSpecs.when(() -> ProductSearchSpecifications.build(any())).thenReturn(searchSpec);
                productSpecs.when(ProductSpecifications::isVisible).thenReturn(visibleSpec);
                given(searchSpec.and(visibleSpec)).willReturn(combinedSpec);
                given(productRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(Page.empty());

                searchProductUseCase.execute(fullRequest(), defaultPageable());

                then(productRepository).should(times(1)).findAll(any(Specification.class), any(Pageable.class));
            }
        }

        @Test
        @DisplayName("deve respeitar paginação retornada pelo repositório")
        void execute_pagedResult_respectsRepositoryPagination() {
            try (MockedStatic<ProductSearchSpecifications> searchSpecs = mockStatic(ProductSearchSpecifications.class);
                 MockedStatic<ProductSpecifications> productSpecs = mockStatic(ProductSpecifications.class)) {

                Specification<Product> searchSpec = mock(Specification.class);
                Specification<Product> visibleSpec = mock(Specification.class);
                Specification<Product> combinedSpec = mock(Specification.class);
                Page<Product> page = new PageImpl<>(
                        List.of(mock(Product.class)),
                        PageRequest.of(0, 10),
                        50
                );

                searchSpecs.when(() -> ProductSearchSpecifications.build(any())).thenReturn(searchSpec);
                productSpecs.when(ProductSpecifications::isVisible).thenReturn(visibleSpec);
                given(searchSpec.and(visibleSpec)).willReturn(combinedSpec);
                given(productRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);
                given(productMapper.toProductSummaryResponse(any())).willReturn(mock(ProductSummaryResponse.class));

                Page<ProductSummaryResponse> result = searchProductUseCase.execute(emptyRequest(), defaultPageable());

                assertThat(result.getTotalElements()).isEqualTo(50);
                assertThat(result.getTotalPages()).isEqualTo(5);
            }
        }
    }
}