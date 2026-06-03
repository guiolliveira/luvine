package com.javacore.spring_api_luvine.product.domain.entity;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.domain.valueObject.AltText;
import com.javacore.spring_api_luvine.product.domain.valueObject.CategoryName;
import com.javacore.spring_api_luvine.product.domain.valueObject.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DisplayName("CategoryImage")
class CategoryImageTest {

    // --- FIXTURE -------------------------------------------------------------

    private static final String IMAGE_URL = "https://cdn.luvine.com/images/roupas.jpg";
    private static final String STORAGE_KEY = "images/roupas.jpg";
    private static final String ALT_TEXT = "Imagem da categoria Roupas";

    private CategoryImage buildImage() {
        return CategoryImage.create(IMAGE_URL, STORAGE_KEY, new AltText(ALT_TEXT));
    }

    private Category buildCategory() {
        return Category.create(null, new CategoryName("Roupas"), new Description("Categoria de roupas"));
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
            CategoryImage a = buildImage();
            CategoryImage b = buildImage();

            assertThat(a.getPublicId()).isNotEqualTo(b.getPublicId());
        }

        @Test
        @DisplayName("deve persistir imageUrl, storageKey e altText corretamente")
        void shouldPersistFields() {
            CategoryImage image = buildImage();

            assertThat(image.getImageUrl()).isEqualTo(IMAGE_URL);
            assertThat(image.getStorageKey()).isEqualTo(STORAGE_KEY);
            assertThat(image.getAltText()).isEqualTo(new AltText(ALT_TEXT));
        }

        @Test
        @DisplayName("deve iniciar sem categoria associada")
        void shouldStartWithNullCategory() {
            assertThat(buildImage().getCategory()).isNull();
        }

        @Test
        @DisplayName("id deve ser nulo antes de persistência (sem banco)")
        void shouldHaveNullIdBeforePersistence() {
            assertThat(buildImage().getId()).isNull();
        }
    }

    // --- ASSIGN TO CATEGORY -------------------------------------------------------------

    @Nested
    @DisplayName("assignToCategory()")
    class AssignToCategory {

        @Test
        @DisplayName("deve associar categoria à imagem")
        void shouldAssignCategoryToImage() {
            CategoryImage image = buildImage();
            Category category = buildCategory();

            image.assignToCategory(category);

            assertThat(image.getCategory()).isEqualTo(category);
        }

        @Test
        @DisplayName("deve substituir categoria existente por nova categoria")
        void shouldReplaceExistingCategory() {
            CategoryImage image = buildImage();
            Category first = buildCategory();
            Category second = Category.create(null, new CategoryName("Calçados"), new Description("Categoria de calçados"));

            image.assignToCategory(first);
            image.assignToCategory(second);

            assertThat(image.getCategory()).isEqualTo(second);
        }

        @Test
        @DisplayName("deve aceitar null para remover associação de categoria")
        void shouldAcceptNullToRemoveCategory() {
            CategoryImage image = buildImage();
            image.assignToCategory(buildCategory());

            image.assignToCategory(null);

            assertThat(image.getCategory()).isNull();
        }
    }

    // --- CHANGE ALT TEXT -------------------------------------------------------------

    @Nested
    @DisplayName("changeAltText()")
    class ChangeAltText {

        @Test
        @DisplayName("deve alterar altText com sucesso quando valor é diferente")
        void shouldChangeAltTextWhenDifferent() {
            CategoryImage image = buildImage();

            image.changeAltText(new AltText("Novo texto alternativo"));

            assertThat(image.getAltText()).isEqualTo(new AltText("Novo texto alternativo"));
        }

        @Test
        @DisplayName("deve lançar UnchangedValueException quando altText é igual ao atual")
        void shouldThrowWhenAltTextIsUnchanged() {
            CategoryImage image = buildImage();

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> image.changeAltText(new AltText(ALT_TEXT)));
        }

        @Test
        @DisplayName("não deve alterar outros campos ao mudar altText")
        void shouldNotAffectOtherFieldsOnAltTextChange() {
            CategoryImage image = buildImage();

            image.changeAltText(new AltText("Outro texto"));

            assertThat(image.getImageUrl()).isEqualTo(IMAGE_URL);
            assertThat(image.getStorageKey()).isEqualTo(STORAGE_KEY);
        }
    }
}