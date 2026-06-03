package com.javacore.spring_api_luvine.product.domain.entity;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.domain.valueObject.AltText;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

@DisplayName("ProductImage")
class ProductImageTest {

    // --- FIXTURE -------------------------------------------------------------

    private static final String IMAGE_URL = "https://cdn.luvine.com/images/camiseta.jpg";
    private static final String STORAGE_KEY = "images/camiseta.jpg";
    private static final String ALT_TEXT = "Imagem da camiseta azul";

    private ProductImage buildImage(boolean primary) {
        return ProductImage.create(IMAGE_URL, STORAGE_KEY, new AltText(ALT_TEXT), primary);
    }

    private ProductImage buildImage() {
        return buildImage(false);
    }

    // --- CREATE() - FACTORY -------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve gerar publicId UUID não nulo")
        void shouldGenerateNonNullPublicId() {
            assertThat(buildImage().getPublicId()).isNotNull();
        }

        @Test
        @DisplayName("cada instância deve ter um publicId único")
        void shouldGenerateDistinctPublicIds() {
            assertThat(buildImage().getPublicId()).isNotEqualTo(buildImage().getPublicId());
        }

        @Test
        @DisplayName("deve persistir imageUrl, storageKey e altText corretamente")
        void shouldPersistFields() {
            ProductImage image = buildImage();

            assertThat(image.getImageUrl()).isEqualTo(IMAGE_URL);
            assertThat(image.getStorageKey()).isEqualTo(STORAGE_KEY);
            assertThat(image.getAltText()).isEqualTo(new AltText(ALT_TEXT));
        }

        @Test
        @DisplayName("deve iniciar com primaryImage = false quando não solicitado")
        void shouldStartAsNonPrimary() {
            assertThat(buildImage(false).isPrimaryImage()).isFalse();
        }

        @Test
        @DisplayName("deve iniciar com primaryImage = true quando solicitado")
        void shouldStartAsPrimaryWhenRequested() {
            assertThat(buildImage(true).isPrimaryImage()).isTrue();
        }

        @Test
        @DisplayName("deve iniciar sem variante associada")
        void shouldStartWithNullVariant() {
            assertThat(buildImage().getProductVariant()).isNull();
        }

        @Test
        @DisplayName("id deve ser nulo antes de persistência")
        void shouldHaveNullIdBeforePersistence() {
            assertThat(buildImage().getId()).isNull();
        }
    }

    // --- ASSIGN / UNASSIGN TO VARIANT -------------------------------------------------------------

    @Nested
    @DisplayName("assignToVariant() e unassignToVariant()")
    class AssignUnassignVariant {

        @Test
        @DisplayName("deve associar variante à imagem")
        void shouldAssignVariantToImage() {
            ProductImage image = buildImage();
            ProductVariant variant = mock(ProductVariant.class);

            image.assignToVariant(variant);

            assertThat(image.getProductVariant()).isEqualTo(variant);
        }

        @Test
        @DisplayName("deve remover associação de variante")
        void shouldUnassignVariant() {
            ProductImage image = buildImage();
            ProductVariant variant = mock(ProductVariant.class);

            image.assignToVariant(variant);
            image.unassignToVariant();

            assertThat(image.getProductVariant()).isNull();
        }

        @Test
        @DisplayName("deve substituir variante existente por nova")
        void shouldReplaceExistingVariant() {
            ProductImage image = buildImage();
            ProductVariant first = mock(ProductVariant.class);
            ProductVariant second = mock(ProductVariant.class);

            image.assignToVariant(first);
            image.assignToVariant(second);

            assertThat(image.getProductVariant()).isEqualTo(second);
        }
    }

    // --- CHANGE ALT TEXT -------------------------------------------------------------

    @Nested
    @DisplayName("changeAltText()")
    class ChangeAltText {

        @Test
        @DisplayName("deve alterar altText com sucesso")
        void shouldChangeAltTextSuccessfully() {
            ProductImage image = buildImage();

            image.changeAltText(new AltText("Novo texto alternativo"));

            assertThat(image.getAltText()).isEqualTo(new AltText("Novo texto alternativo"));
        }

        @Test
        @DisplayName("deve lançar UnchangedValueException quando altText é igual ao atual")
        void shouldThrowWhenAltTextIsUnchanged() {
            ProductImage image = buildImage();

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> image.changeAltText(new AltText(ALT_TEXT)));
        }

        @Test
        @DisplayName("não deve alterar outros campos ao mudar altText")
        void shouldNotAffectOtherFields() {
            ProductImage image = buildImage();

            image.changeAltText(new AltText("Outro texto"));

            assertThat(image.getImageUrl()).isEqualTo(IMAGE_URL);
            assertThat(image.getStorageKey()).isEqualTo(STORAGE_KEY);
        }
    }

    // --- CHANGE DISPLAY ORDER -------------------------------------------------------------

    @Nested
    @DisplayName("changeDisplayOrder()")
    class ChangeDisplayOrder {

        @Test
        @DisplayName("deve alterar displayOrder com sucesso")
        void shouldChangeDisplayOrderSuccessfully() {
            ProductImage image = buildImage();

            image.changeDisplayOrder(3);

            assertThat(image.getDisplayOrder()).isEqualTo(3);
        }

        @Test
        @DisplayName("deve aceitar displayOrder zero")
        void shouldAcceptZeroDisplayOrder() {
            ProductImage image = buildImage();

            image.changeDisplayOrder(0);

            assertThat(image.getDisplayOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("deve sobrescrever displayOrder anterior")
        void shouldOverwritePreviousDisplayOrder() {
            ProductImage image = buildImage();
            image.changeDisplayOrder(1);

            image.changeDisplayOrder(5);

            assertThat(image.getDisplayOrder()).isEqualTo(5);
        }
    }

    // --- SET / UNSET PRIMARY -------------------------------------------------------------

    @Nested
    @DisplayName("setAsPrimary() e unsetAsPrimary()")
    class SetUnsetPrimary {

        @Test
        @DisplayName("deve marcar imagem como primária")
        void shouldSetAsPrimary() {
            ProductImage image = buildImage(false);

            image.setAsPrimary();

            assertThat(image.isPrimaryImage()).isTrue();
        }

        @Test
        @DisplayName("deve desmarcar imagem como primária")
        void shouldUnsetAsPrimary() {
            ProductImage image = buildImage(true);

            image.unsetAsPrimary();

            assertThat(image.isPrimaryImage()).isFalse();
        }

        @Test
        @DisplayName("setAsPrimary() deve ser idempotente")
        void setAsPrimaryShouldBeIdempotent() {
            ProductImage image = buildImage(true);

            image.setAsPrimary();

            assertThat(image.isPrimaryImage()).isTrue();
        }

        @Test
        @DisplayName("unsetAsPrimary() deve ser idempotente")
        void unsetAsPrimaryShouldBeIdempotent() {
            ProductImage image = buildImage(false);

            image.unsetAsPrimary();

            assertThat(image.isPrimaryImage()).isFalse();
        }
    }
}