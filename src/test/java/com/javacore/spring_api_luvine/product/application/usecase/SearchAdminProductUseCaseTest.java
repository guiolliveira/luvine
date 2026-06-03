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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("SearchAdminProductUseCase")
@ExtendWith(MockitoExtension.class)
class SearchAdminProductUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private SearchAdminProductUseCase searchAdminProductUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final Pageable PAGEABLE = PageRequest.of(0, 10);

    private SearchProductRequest validRequest() {
        return new SearchProductRequest(null, null, null, null, null, null, null);
    }

    private SearchProductRequest requestWithFilters() {
        return new SearchProductRequest("camisas", "Camiseta", "Azul", "M", null, null, Status.ACTIVE);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve retornar Page de ProductSummaryResponse com os resultados mapeados")
        void execute_validRequest_returnsMappedPage() {
            Product product = mock(Product.class);
            ProductSummaryResponse response = mock(ProductSummaryResponse.class);
            Page<Product> productPage = new PageImpl<>(List.of(product), PAGEABLE, 1);

            try (MockedStatic<ProductSearchSpecifications> searchSpecs = mockStatic(ProductSearchSpecifications.class);
                 MockedStatic<ProductSpecifications> productSpecs = mockStatic(ProductSpecifications.class)) {

                Specification<Product> searchSpec = mock(Specification.class);
                Specification<Product> visibleSpec = mock(Specification.class);
                Specification<Product> combinedSpec = mock(Specification.class);

                searchSpecs.when(() -> ProductSearchSpecifications.build(any())).thenReturn(searchSpec);
                productSpecs.when(ProductSpecifications::isVisible).thenReturn(visibleSpec);
                given(searchSpec.and(visibleSpec)).willReturn(combinedSpec);
                given(productRepository.findAll(combinedSpec, PAGEABLE)).willReturn(productPage);
                given(productMapper.toProductSummaryResponse(product)).willReturn(response);

                Page<ProductSummaryResponse> result = searchAdminProductUseCase.execute(validRequest(), PAGEABLE);

                assertThat(result).isNotNull();
                assertThat(result.getContent()).containsExactly(response);
            }
        }

        @Test
        @DisplayName("deve retornar Page vazia quando nenhum produto encontrado")
        void execute_noResults_returnsEmptyPage() {
            Page<Product> emptyPage = new PageImpl<>(List.of(), PAGEABLE, 0);

            try (MockedStatic<ProductSearchSpecifications> searchSpecs = mockStatic(ProductSearchSpecifications.class);
                 MockedStatic<ProductSpecifications> productSpecs = mockStatic(ProductSpecifications.class)) {

                Specification<Product> searchSpec = mock(Specification.class);
                Specification<Product> visibleSpec = mock(Specification.class);
                Specification<Product> combinedSpec = mock(Specification.class);

                searchSpecs.when(() -> ProductSearchSpecifications.build(any())).thenReturn(searchSpec);
                productSpecs.when(ProductSpecifications::isVisible).thenReturn(visibleSpec);
                given(searchSpec.and(visibleSpec)).willReturn(combinedSpec);
                given(productRepository.findAll(combinedSpec, PAGEABLE)).willReturn(emptyPage);

                Page<ProductSummaryResponse> result = searchAdminProductUseCase.execute(validRequest(), PAGEABLE);

                assertThat(result).isNotNull();
                assertThat(result.getContent()).isEmpty();
                assertThat(result.getTotalElements()).isZero();
            }
        }

        @Test
        @DisplayName("deve construir specification a partir do request com filtro isVisible")
        void execute_validRequest_buildsSpecificationWithIsVisibleFilter() {
            SearchProductRequest request = requestWithFilters();
            Page<Product> emptyPage = new PageImpl<>(List.of(), PAGEABLE, 0);

            try (MockedStatic<ProductSearchSpecifications> searchSpecs = mockStatic(ProductSearchSpecifications.class);
                 MockedStatic<ProductSpecifications> productSpecs = mockStatic(ProductSpecifications.class)) {

                Specification<Product> searchSpec = mock(Specification.class);
                Specification<Product> visibleSpec = mock(Specification.class);
                Specification<Product> combinedSpec = mock(Specification.class);

                searchSpecs.when(() -> ProductSearchSpecifications.build(request)).thenReturn(searchSpec);
                productSpecs.when(ProductSpecifications::isVisible).thenReturn(visibleSpec);
                given(searchSpec.and(visibleSpec)).willReturn(combinedSpec);
                given(productRepository.findAll(combinedSpec, PAGEABLE)).willReturn(emptyPage);

                searchAdminProductUseCase.execute(request, PAGEABLE);

                searchSpecs.verify(() -> ProductSearchSpecifications.build(request));
                productSpecs.verify(ProductSpecifications::isVisible);
            }
        }

        @Test
        @DisplayName("deve mapear cada produto retornado pelo repositório")
        void execute_multipleResults_mapsEachProduct() {
            Product product1 = mock(Product.class);
            Product product2 = mock(Product.class);
            Page<Product> productPage = new PageImpl<>(List.of(product1, product2), PAGEABLE, 2);

            try (MockedStatic<ProductSearchSpecifications> searchSpecs = mockStatic(ProductSearchSpecifications.class);
                 MockedStatic<ProductSpecifications> productSpecs = mockStatic(ProductSpecifications.class)) {

                Specification<Product> searchSpec = mock(Specification.class);
                Specification<Product> visibleSpec = mock(Specification.class);
                Specification<Product> combinedSpec = mock(Specification.class);

                searchSpecs.when(() -> ProductSearchSpecifications.build(any())).thenReturn(searchSpec);
                productSpecs.when(ProductSpecifications::isVisible).thenReturn(visibleSpec);
                given(searchSpec.and(visibleSpec)).willReturn(combinedSpec);
                given(productRepository.findAll(combinedSpec, PAGEABLE)).willReturn(productPage);
                given(productMapper.toProductSummaryResponse(any(Product.class)))
                        .willReturn(mock(ProductSummaryResponse.class));

                searchAdminProductUseCase.execute(validRequest(), PAGEABLE);

                then(productMapper).should().toProductSummaryResponse(product1);
                then(productMapper).should().toProductSummaryResponse(product2);
            }
        }

        @Test
        @DisplayName("deve preservar metadados de paginação no resultado")
        void execute_validRequest_preservesPaginationMetadata() {
            Pageable pageable = PageRequest.of(1, 20);
            Page<Product> productPage = new PageImpl<>(List.of(), pageable, 45);

            try (MockedStatic<ProductSearchSpecifications> searchSpecs = mockStatic(ProductSearchSpecifications.class);
                 MockedStatic<ProductSpecifications> productSpecs = mockStatic(ProductSpecifications.class)) {

                Specification<Product> searchSpec = mock(Specification.class);
                Specification<Product> visibleSpec = mock(Specification.class);
                Specification<Product> combinedSpec = mock(Specification.class);

                searchSpecs.when(() -> ProductSearchSpecifications.build(any())).thenReturn(searchSpec);
                productSpecs.when(ProductSpecifications::isVisible).thenReturn(visibleSpec);
                given(searchSpec.and(visibleSpec)).willReturn(combinedSpec);
                given(productRepository.findAll(combinedSpec, pageable)).willReturn(productPage);

                Page<ProductSummaryResponse> result = searchAdminProductUseCase.execute(validRequest(), pageable);

                assertThat(result.getTotalElements()).isEqualTo(45);
                assertThat(result.getNumber()).isEqualTo(1);
                assertThat(result.getSize()).isEqualTo(20);
            }
        }
    }
}