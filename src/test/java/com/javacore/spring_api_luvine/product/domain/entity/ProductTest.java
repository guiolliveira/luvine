package com.javacore.spring_api_luvine.product.domain.entity;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.domain.exception.DuplicatedSkuException;
import com.javacore.spring_api_luvine.product.domain.exception.VariantAlreadyExistsException;
import com.javacore.spring_api_luvine.product.domain.exception.VariantNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("Product")
class ProductTest {

    // --- FIXTURE -------------------------------------------------------------

    private static final String PRODUCT_NAME = "Camiseta Básica";
    private static final String DESCRIPTION = "Camiseta de algodão";
    private static final BigDecimal BASE_PRICE = new BigDecimal("99.90");

    private Category buildCategory(String name) {
        return Category.create(
                null,
                new CategoryName(name),
                new Description("Descrição da categoria")
        );
    }

    private Category buildCategory() {
        return buildCategory("Roupas");
    }

    private Product buildProduct() {
        return Product.create(
                buildCategory(),
                new ProductName(PRODUCT_NAME),
                new Description(DESCRIPTION),
                new Money(BASE_PRICE)
        );
    }

    private ProductVariant buildVariant(String color, String size, String sku) {
        ProductVariant variant = mock(ProductVariant.class);
        given(variant.getPublicId()).willReturn(UUID.randomUUID());
        given(variant.getColor()).willReturn(new Color(color));
        given(variant.getSize()).willReturn(new Size(size));
        given(variant.getSku()).willReturn(new Sku(sku));
        return variant;
    }

    // --- CREATE() - FACTORY -------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve gerar publicId UUID não nulo")
        void shouldGenerateNonNullPublicId() {
            assertThat(buildProduct().getPublicId()).isNotNull();
        }

        @Test
        @DisplayName("cada instância deve ter publicId único")
        void shouldGenerateDistinctPublicIds() {
            assertThat(buildProduct().getPublicId()).isNotEqualTo(buildProduct().getPublicId());
        }

        @Test
        @DisplayName("deve persistir campos corretamente")
        void shouldPersistFields() {
            Product product = buildProduct();

            assertThat(product.getProductName()).isEqualTo(new ProductName(PRODUCT_NAME));
            assertThat(product.getDescription()).isEqualTo(new Description(DESCRIPTION));
            assertThat(product.getBasePrice()).isEqualTo(new Money(BASE_PRICE));
            assertThat(product.getCategory()).isNotNull();
        }

        @Test
        @DisplayName("deve gerar slug a partir do productName")
        void shouldGenerateSlugFromProductName() {
            Product product = buildProduct();

            assertThat(product.getSlug()).isEqualTo(new Slug(PRODUCT_NAME));
        }

        @Test
        @DisplayName("deve iniciar com status ACTIVE")
        void shouldStartWithActiveStatus() {
            assertThat(buildProduct().getStatus()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("deve iniciar com conjunto de variantes vazio")
        void shouldStartWithEmptyVariants() {
            assertThat(buildProduct().getVariants()).isEmpty();
        }

        @Test
        @DisplayName("id deve ser nulo antes de persistência")
        void shouldHaveNullIdBeforePersistence() {
            assertThat(buildProduct().getId()).isNull();
        }
    }

    // --- ADD VARIANT -------------------------------------------------------------

    @Nested
    @DisplayName("addVariant()")
    class AddVariant {

        @Test
        @DisplayName("deve adicionar variante com sucesso")
        void shouldAddVariantSuccessfully() {
            Product product = buildProduct();
            ProductVariant variant = buildVariant("Azul", "M", "SKU-001");

            product.addVariant(variant);

            assertThat(product.getVariants()).contains(variant);
        }

        @Test
        @DisplayName("deve lançar VariantAlreadyExistsException quando cor e tamanho já existem")
        void shouldThrowWhenVariantAlreadyExists() {
            Product product = buildProduct();
            ProductVariant first = buildVariant("Azul", "M", "SKU-001");
            ProductVariant duplicate = buildVariant("Azul", "M", "SKU-002");

            product.addVariant(first);

            assertThatExceptionOfType(VariantAlreadyExistsException.class)
                    .isThrownBy(() -> product.addVariant(duplicate));
        }

        @Test
        @DisplayName("deve lançar DuplicatedSkuException quando SKU já existe")
        void shouldThrowWhenSkuAlreadyExists() {
            Product product = buildProduct();
            ProductVariant first = buildVariant("Azul", "M", "SKU-001");
            ProductVariant sameSku = buildVariant("Vermelho", "G", "SKU-001");

            product.addVariant(first);

            assertThatExceptionOfType(DuplicatedSkuException.class)
                    .isThrownBy(() -> product.addVariant(sameSku));
        }

        @Test
        @DisplayName("deve permitir adicionar variantes com cores ou tamanhos diferentes")
        void shouldAllowVariantsWithDifferentColorOrSize() {
            Product product = buildProduct();
            ProductVariant v1 = buildVariant("Azul", "M", "SKU-001");
            ProductVariant v2 = buildVariant("Azul", "G", "SKU-002");
            ProductVariant v3 = buildVariant("Vermelho", "M", "SKU-003");

            assertThatNoException().isThrownBy(() -> {
                product.addVariant(v1);
                product.addVariant(v2);
                product.addVariant(v3);
            });

            assertThat(product.getVariants()).hasSize(3);
        }
    }

    // --- REMOVE VARIANT -------------------------------------------------------------

    @Nested
    @DisplayName("removeVariant()")
    class RemoveVariant {

        @Test
        @DisplayName("deve remover variante com sucesso")
        void shouldRemoveVariantSuccessfully() {
            Product product = buildProduct();
            ProductVariant variant = buildVariant("Azul", "M", "SKU-001");
            product.addVariant(variant);

            product.removeVariant(variant);

            assertThat(product.getVariants()).doesNotContain(variant);
        }
    }

    // --- FIND VARIANT BY PUBLIC ID -------------------------------------------------------------

    @Nested
    @DisplayName("findVariantByPublicId()")
    class FindVariantByPublicId {

        @Test
        @DisplayName("deve retornar variante quando publicId existe")
        void shouldReturnVariantWhenFound() {
            Product product = buildProduct();
            ProductVariant variant = buildVariant("Azul", "M", "SKU-001");
            UUID publicId = variant.getPublicId();
            product.addVariant(variant);

            assertThat(product.findVariantByPublicId(publicId)).isEqualTo(variant);
        }

        @Test
        @DisplayName("deve lançar VariantNotFoundException quando publicId não existe")
        void shouldThrowWhenVariantNotFound() {
            Product product = buildProduct();

            assertThatExceptionOfType(VariantNotFoundException.class)
                    .isThrownBy(() -> product.findVariantByPublicId(UUID.randomUUID()));
        }
    }

    // --- UPDATE VARIANT ATTRIBUTES -------------------------------------------------------------

    @Nested
    @DisplayName("updateVariantAttributes()")
    class UpdateVariantAttributes {

        @Test
        @DisplayName("deve lançar VariantNotFoundException quando variante não existe")
        void shouldThrowWhenVariantNotFound() {
            Product product = buildProduct();

            assertThatExceptionOfType(VariantNotFoundException.class)
                    .isThrownBy(() -> product.updateVariantAttributes(
                            UUID.randomUUID(), new Color("Azul"), new Size("M")));
        }

        @Test
        @DisplayName("deve lançar VariantAlreadyExistsException quando cor e tamanho já pertencem a outra variante")
        void shouldThrowWhenUpdatedAttributesConflictWithExistingVariant() {
            Product product = buildProduct();
            ProductVariant v1 = buildVariant("Azul", "M", "SKU-001");
            ProductVariant v2 = buildVariant("Vermelho", "G", "SKU-002");
            UUID v2Id = v2.getPublicId();

            product.addVariant(v1);
            product.addVariant(v2);

            assertThatExceptionOfType(VariantAlreadyExistsException.class)
                    .isThrownBy(() -> product.updateVariantAttributes(v2Id, new Color("Azul"), new Size("M")));
        }
    }

    // --- CHANGE CATEGORY -------------------------------------------------------------

    @Nested
    @DisplayName("changeCategory()")
    class ChangeCategory {

        @Test
        @DisplayName("deve alterar categoria com sucesso")
        void shouldChangeCategorySuccessfully() {
            Product product = buildProduct();
            Category newCategory = buildCategory("Calçados");

            product.changeCategory(newCategory);

            assertThat(product.getCategory()).isEqualTo(newCategory);
        }

        @Test
        @DisplayName("deve lançar UnchangedValueException quando categoria é igual à atual")
        void shouldThrowWhenCategoryIsUnchanged() {
            Product product = buildProduct();

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> product.changeCategory(product.getCategory()));
        }
    }

    // --- RENAME -------------------------------------------------------------

    @Nested
    @DisplayName("rename()")
    class Rename {

        @Test
        @DisplayName("deve renomear produto e atualizar slug")
        void shouldRenameAndUpdateSlug() {
            Product product = buildProduct();

            product.rename(new ProductName("Camiseta Premium"));

            assertThat(product.getProductName()).isEqualTo(new ProductName("Camiseta Premium"));
            assertThat(product.getSlug()).isEqualTo(new Slug("Camiseta Premium"));
        }

        @Test
        @DisplayName("deve lançar UnchangedValueException quando nome é igual ao atual")
        void shouldThrowWhenNameIsUnchanged() {
            Product product = buildProduct();

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> product.rename(new ProductName(PRODUCT_NAME)));
        }
    }

    // --- CHANGE DESCRIPTION -------------------------------------------------------------

    @Nested
    @DisplayName("changeDescription()")
    class ChangeDescription {

        @Test
        @DisplayName("deve alterar descrição com sucesso")
        void shouldChangeDescriptionSuccessfully() {
            Product product = buildProduct();

            product.changeDescription(new Description("Nova descrição do produto"));

            assertThat(product.getDescription()).isEqualTo(new Description("Nova descrição do produto"));
        }

        @Test
        @DisplayName("deve lançar UnchangedValueException quando descrição é igual à atual")
        void shouldThrowWhenDescriptionIsUnchanged() {
            Product product = buildProduct();

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> product.changeDescription(new Description(DESCRIPTION)));
        }
    }

    // --- CHANGE BASE PRICE -------------------------------------------------------------

    @Nested
    @DisplayName("changeBasePrice()")
    class ChangeBasePrice {

        @Test
        @DisplayName("deve alterar preço base com sucesso")
        void shouldChangeBasePriceSuccessfully() {
            Product product = buildProduct();

            product.changeBasePrice(new Money(new BigDecimal("149.90")));

            assertThat(product.getBasePrice()).isEqualTo(new Money(new BigDecimal("149.90")));
        }

        @Test
        @DisplayName("deve lançar UnchangedValueException quando preço é igual ao atual")
        void shouldThrowWhenPriceIsUnchanged() {
            Product product = buildProduct();

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> product.changeBasePrice(new Money(BASE_PRICE)));
        }
    }

    // --- CHANGE STATUS -------------------------------------------------------------

    @Nested
    @DisplayName("changeStatus()")
    class ChangeStatus {

        @Test
        @DisplayName("deve alterar status com sucesso")
        void shouldChangeStatusSuccessfully() {
            Product product = buildProduct();

            product.changeStatus(Status.INACTIVE);

            assertThat(product.getStatus()).isEqualTo(Status.INACTIVE);
        }

        @Test
        @DisplayName("deve lançar UnchangedValueException quando status é igual ao atual")
        void shouldThrowWhenStatusIsUnchanged() {
            Product product = buildProduct();

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> product.changeStatus(Status.ACTIVE));
        }

        @Test
        @DisplayName("deve aceitar todos os status válidos")
        void shouldAcceptAllValidStatuses() {
            assertThatNoException().isThrownBy(() -> {
                Product p1 = buildProduct();
                p1.changeStatus(Status.INACTIVE);

                Product p2 = buildProduct();
                p2.changeStatus(Status.DRAFT);

                Product p3 = buildProduct();
                p3.changeStatus(Status.ARCHIVED);
            });
        }
    }
}