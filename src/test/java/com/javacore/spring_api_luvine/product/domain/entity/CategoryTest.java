package com.javacore.spring_api_luvine.product.domain.entity;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.domain.exception.*;
import com.javacore.spring_api_luvine.product.domain.valueObject.CategoryName;
import com.javacore.spring_api_luvine.product.domain.valueObject.Description;
import com.javacore.spring_api_luvine.product.domain.valueObject.Slug;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("Category")
class CategoryTest {

    // --- FIXTURE -------------------------------------------------------------

    private static final String CATEGORY_NAME = "Roupas";
    private static final String DESCRIPTION = "Categoria de roupas em geral";

    private Category buildCategory(String name) {
        return Category.create(null, new CategoryName(name), new Description(DESCRIPTION));
    }

    private Category buildCategory() {
        return buildCategory(CATEGORY_NAME);
    }

    // --- CREATE() - FACTORY -------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve gerar publicId UUID não nulo")
        void shouldGenerateNonNullPublicId() {
            assertThat(buildCategory().getPublicId()).isNotNull();
        }

        @Test
        @DisplayName("cada instância deve ter um publicId único")
        void shouldGenerateDistinctPublicIds() {
            Category a = buildCategory();
            Category b = buildCategory("Calçados");

            assertThat(a.getPublicId()).isNotEqualTo(b.getPublicId());
        }

        @Test
        @DisplayName("deve persistir categoryName, description e parent corretamente")
        void shouldPersistFields() {
            Category parent = buildCategory("Moda");
            Category category = Category.create(parent, new CategoryName(CATEGORY_NAME), new Description(DESCRIPTION));

            assertThat(category.getCategoryName()).isEqualTo(new CategoryName(CATEGORY_NAME));
            assertThat(category.getDescription()).isEqualTo(new Description(DESCRIPTION));
            assertThat(category.getParent()).isEqualTo(parent);
        }

        @Test
        @DisplayName("deve iniciar com active = true")
        void shouldStartActive() {
            assertThat(buildCategory().isActive()).isTrue();
        }

        @Test
        @DisplayName("deve gerar slug a partir do categoryName")
        void shouldGenerateSlugFromCategoryName() {
            Category category = buildCategory();

            assertThat(category.getSlug()).isEqualTo(new Slug(CATEGORY_NAME));
        }

        @Test
        @DisplayName("deve iniciar sem imagem")
        void shouldStartWithNullImage() {
            assertThat(buildCategory().getImage()).isNull();
        }

        @Test
        @DisplayName("deve aceitar parent nulo")
        void shouldAcceptNullParent() {
            Category category = Category.create(null, new CategoryName(CATEGORY_NAME), new Description(DESCRIPTION));

            assertThat(category.getParent()).isNull();
        }

        @Test
        @DisplayName("id deve ser nulo antes de persistência (sem banco)")
        void shouldHaveNullIdBeforePersistence() {
            assertThat(buildCategory().getId()).isNull();
        }
    }

    // --- ADD CATEGORY IMAGE -------------------------------------------------------------

    @Nested
    @DisplayName("addCategoryImage()")
    class AddCategoryImage {

        @Test
        @DisplayName("deve associar imagem à categoria")
        void shouldAssignImageToCategory() {
            Category category = buildCategory();
            CategoryImage image = mock(CategoryImage.class);

            category.addCategoryImage(image);

            assertThat(category.getImage()).isEqualTo(image);
        }

        @Test
        @DisplayName("deve lançar NullPointerException quando imagem é nula")
        void shouldThrowWhenImageIsNull() {
            Category category = buildCategory();

            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> category.addCategoryImage(null));
        }

        @Test
        @DisplayName("deve substituir imagem existente por nova imagem")
        void shouldReplaceExistingImage() {
            Category category = buildCategory();
            CategoryImage firstImage  = mock(CategoryImage.class);
            CategoryImage secondImage = mock(CategoryImage.class);

            category.addCategoryImage(firstImage);
            category.addCategoryImage(secondImage);

            assertThat(category.getImage()).isEqualTo(secondImage);
        }
    }

    // --- FIND IMAGE BY PUBLIC ID -------------------------------------------------------------

    @Nested
    @DisplayName("findImageByPublicId()")
    class FindImageByPublicId {

        @Test
        @DisplayName("deve retornar imagem quando publicId corresponde")
        void shouldReturnImageWhenPublicIdMatches() {
            Category category = buildCategory();
            UUID imagePublicId = UUID.randomUUID();
            CategoryImage image = mock(CategoryImage.class);
            given(image.getPublicId()).willReturn(imagePublicId);

            category.addCategoryImage(image);

            assertThat(category.findImageByPublicId(imagePublicId)).isEqualTo(image);
        }

        @Test
        @DisplayName("deve lançar CategoryImageNotFoundException quando publicId não corresponde")
        void shouldThrowWhenPublicIdDoesNotMatch() {
            Category category = buildCategory();
            CategoryImage image = mock(CategoryImage.class);
            given(image.getPublicId()).willReturn(UUID.randomUUID());

            category.addCategoryImage(image);

            assertThatExceptionOfType(CategoryImageNotFoundException.class)
                    .isThrownBy(() -> category.findImageByPublicId(UUID.randomUUID()));
        }

        @Test
        @DisplayName("deve lançar NullPointerException quando não há imagem associada")
        void shouldThrowWhenNoImageAssigned() {
            Category category = buildCategory();

            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> category.findImageByPublicId(UUID.randomUUID()));
        }
    }

    // --- CHANGE PARENT -------------------------------------------------------------

    @Nested
    @DisplayName("changeParent()")
    class ChangeParent {

        @Test
        @DisplayName("deve alterar parent com sucesso")
        void shouldChangeParentSuccessfully() {
            Category category = buildCategory();
            Category newParent = buildCategory("Moda");

            category.changeParent(newParent);

            assertThat(category.getParent()).isEqualTo(newParent);
        }

        @Test
        @DisplayName("deve aceitar null para remover parent")
        void shouldAcceptNullToRemoveParent() {
            Category parent   = buildCategory("Moda");
            Category category = Category.create(parent, new CategoryName(CATEGORY_NAME), new Description(DESCRIPTION));

            category.changeParent(null);

            assertThat(category.getParent()).isNull();
        }

        @Test
        @DisplayName("deve lançar InvalidCategoryHierarchyException quando categoria tenta ser seu próprio parent")
        void shouldThrowWhenSettingSelfAsParent() {
            Category category = buildCategory();

            assertThatExceptionOfType(InvalidCategoryHierarchyException.class)
                    .isThrownBy(() -> category.changeParent(category));
        }

        @Test
        @DisplayName("deve lançar CircularHierarchyDetectedException quando detectado ciclo na hierarquia")
        void shouldThrowWhenCircularHierarchyDetected() {
            Category grandParent = buildCategory("Moda");
            Category parent      = buildCategory("Feminino");
            Category child       = buildCategory();

            parent.changeParent(grandParent);
            child.changeParent(parent);

            assertThatExceptionOfType(CircularHierarchyDetectedException.class)
                    .isThrownBy(() -> grandParent.changeParent(child));
        }
    }

    // --- RENAME -------------------------------------------------------------

    @Nested
    @DisplayName("rename()")
    class Rename {

        @Test
        @DisplayName("deve renomear categoria com sucesso e atualizar slug")
        void shouldRenameAndUpdateSlug() {
            Category category = buildCategory();

            category.rename(new CategoryName("Calçados"));

            assertThat(category.getCategoryName()).isEqualTo(new CategoryName("Calçados"));
            assertThat(category.getSlug()).isEqualTo(new Slug("Calçados"));
        }

        @Test
        @DisplayName("deve lançar UnchangedValueException quando nome é igual ao atual")
        void shouldThrowWhenNameIsUnchanged() {
            Category category = buildCategory();

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> category.rename(new CategoryName(CATEGORY_NAME)));
        }
    }

    // --- CHANGE DESCRIPTION -------------------------------------------------------------

    @Nested
    @DisplayName("changeDescription()")
    class ChangeDescription {

        @Test
        @DisplayName("deve alterar descrição com sucesso")
        void shouldChangeDescriptionSuccessfully() {
            Category category = buildCategory();

            category.changeDescription(new Description("Nova descrição da categoria"));

            assertThat(category.getDescription()).isEqualTo(new Description("Nova descrição da categoria"));
        }

        @Test
        @DisplayName("deve lançar UnchangedValueException quando descrição é igual à atual")
        void shouldThrowWhenDescriptionIsUnchanged() {
            Category category = buildCategory();

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> category.changeDescription(new Description(DESCRIPTION)));
        }
    }

    // --- ACTIVATE / DEACTIVATE -------------------------------------------------------------

    @Nested
    @DisplayName("activate() e deactivate()")
    class ActivateDeactivate {

        @Test
        @DisplayName("deve desativar categoria ativa")
        void shouldDeactivateActiveCategory() {
            Category category = buildCategory();

            category.deactivate();

            assertThat(category.isActive()).isFalse();
        }

        @Test
        @DisplayName("deve ativar categoria inativa")
        void shouldActivateInactiveCategory() {
            Category category = buildCategory();
            category.deactivate();

            category.activate();

            assertThat(category.isActive()).isTrue();
        }

        @Test
        @DisplayName("deve lançar CategoryAlreadyActivateException quando já está ativa")
        void shouldThrowWhenAlreadyActive() {
            Category category = buildCategory();

            assertThatExceptionOfType(CategoryAlreadyActivateException.class)
                    .isThrownBy(category::activate);
        }

        @Test
        @DisplayName("deve lançar CategoryAlreadyDeactivateException quando já está inativa")
        void shouldThrowWhenAlreadyInactive() {
            Category category = buildCategory();
            category.deactivate();

            assertThatExceptionOfType(CategoryAlreadyDeactivateException.class)
                    .isThrownBy(category::deactivate);
        }

        @Test
        @DisplayName("deve ser possível ciclar entre ativo e inativo sem erros")
        void shouldCycleBetweenActiveAndInactiveWithoutError() {
            Category category = buildCategory();

            assertThatNoException().isThrownBy(() -> {
                category.deactivate();
                category.activate();
                category.deactivate();
                category.activate();
            });
        }
    }
}