package com.javacore.spring_api_luvine.product.domain.entity;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.domain.exception.*;
import com.javacore.spring_api_luvine.product.domain.valueObject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;

@DisplayName("ProductVariant")
class ProductVariantTest {

    // --- HELPERS ------------------------------------------------------------------

    private static final Color COLOR = new Color("Azul");
    private static final Size SIZE = new Size("M");
    private static final Money PRICE = new Money(new BigDecimal("99.90"));
    private static final StockQuantity STOCK = new StockQuantity(10);

    private ProductVariant buildVariant() {
        return ProductVariant.create(COLOR, SIZE, PRICE, STOCK);
    }

    private ProductImage mockImage(boolean primary, int displayOrder) {
        ProductImage image = mock(ProductImage.class);
        given(image.isPrimaryImage()).willReturn(primary);
        given(image.getDisplayOrder()).willReturn(displayOrder);
        given(image.getPublicId()).willReturn(UUID.randomUUID());
        return image;
    }

    // --- CREATE ------------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve criar variante com dados válidos")
        void create_validData_createsVariant() {
            ProductVariant variant = buildVariant();

            assertThat(variant).isNotNull();
            assertThat(variant.getPublicId()).isNotNull();
            assertThat(variant.getColor()).isEqualTo(COLOR);
            assertThat(variant.getSize()).isEqualTo(SIZE);
            assertThat(variant.getPrice()).isEqualTo(PRICE);
            assertThat(variant.getStockQuantity()).isEqualTo(STOCK);
            assertThat(variant.isActive()).isTrue();
            assertThat(variant.getImages()).isEmpty();
        }

        @Test
        @DisplayName("deve gerar publicId único a cada criação")
        void create_calledTwice_generatesDifferentPublicIds() {
            ProductVariant a = buildVariant();
            ProductVariant b = buildVariant();
            assertThat(a.getPublicId()).isNotEqualTo(b.getPublicId());
        }

        @Test
        @DisplayName("deve gerar SKU automaticamente na criação")
        void create_validData_generatesSku() {
            ProductVariant variant = buildVariant();
            assertThat(variant.getSku()).isNotNull();
            assertThat(variant.getSku().value()).isNotBlank();
        }
    }

    // --- ADD IMAGE ---------------------------------------------------------------

    @Nested
    @DisplayName("addImage()")
    class AddImage {

        @Test
        @DisplayName("deve adicionar imagem à lista")
        void addImage_validImage_addsToList() {
            ProductVariant variant = buildVariant();
            ProductImage image = mockImage(false, 1);

            variant.addImage(image, 1);

            assertThat(variant.getImages()).hasSize(1);
        }

        @Test
        @DisplayName("deve definir primeira imagem adicionada como primária automaticamente")
        void addImage_firstImage_setsAsPrimary() {
            ProductVariant variant = buildVariant();
            ProductImage image = mockImage(false, 1);

            variant.addImage(image, null);

            then(image).should().setAsPrimary();
        }

        @Test
        @DisplayName("deve lançar ImageLimitExceededException ao adicionar mais de 10 imagens")
        void addImage_limitExceeded_throwsImageLimitExceededException() {
            ProductVariant variant = buildVariant();

            for (int i = 1; i <= 10; i++) {
                ProductImage img = mockImage(false, i);
                variant.addImage(img, null);
            }

            assertThatExceptionOfType(ImageLimitExceededException.class)
                    .isThrownBy(() -> variant.addImage(mockImage(false, 11), null));
        }

        @Test
        @DisplayName("deve lançar NullPointerException quando imagem é nula")
        void addImage_nullImage_throwsNullPointerException() {
            ProductVariant variant = buildVariant();

            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> variant.addImage(null, 1));
        }

        @Test
        @DisplayName("deve lançar InvalidImageReorderException quando displayOrder é inválido")
        void addImage_invalidDisplayOrder_throwsInvalidImageReorderException() {
            ProductVariant variant = buildVariant();
            ProductImage image = mockImage(false, 5);

            assertThatExceptionOfType(InvalidImageReorderException.class)
                    .isThrownBy(() -> variant.addImage(image, 5));
        }

        @Test
        @DisplayName("deve desmarcar imagens primárias existentes ao adicionar imagem marcada como primária")
        void addImage_primaryImage_unsetsOtherPrimaries() {
            ProductVariant variant = buildVariant();
            ProductImage first = mockImage(false, 1);
            variant.addImage(first, null);

            ProductImage newPrimary = mockImage(true, 1);
            variant.addImage(newPrimary, 1);

            then(first).should().unsetAsPrimary();
        }

        @Test
        @DisplayName("deve chamar assignToVariant na imagem adicionada")
        void addImage_validImage_assignsVariantToImage() {
            ProductVariant variant = buildVariant();
            ProductImage image = mockImage(false, 1);

            variant.addImage(image, null);

            then(image).should().assignToVariant(variant);
        }
    }

    // --- REMOVE IMAGE ------------------------------------------------------------

    @Nested
    @DisplayName("removeImage()")
    class RemoveImage {

        @Test
        @DisplayName("deve remover imagem da lista")
        void removeImage_existingImage_removesFromList() {
            ProductVariant variant = buildVariant();
            ProductImage image = mockImage(false, 1);
            variant.addImage(image, null);

            variant.removeImage(image);

            assertThat(variant.getImages()).isEmpty();
        }

        @Test
        @DisplayName("deve lançar ImageNotFoundException quando imagem não pertence à variante")
        void removeImage_imageNotInVariant_throwsImageNotFoundException() {
            ProductVariant variant = buildVariant();
            ProductImage outsider = mockImage(false, 1);

            assertThatExceptionOfType(ImageNotFoundException.class)
                    .isThrownBy(() -> variant.removeImage(outsider));
        }

        @Test
        @DisplayName("deve lançar NullPointerException quando imagem é nula")
        void removeImage_nullImage_throwsNullPointerException() {
            ProductVariant variant = buildVariant();

            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> variant.removeImage(null));
        }

        @Test
        @DisplayName("deve promover próxima imagem como primária ao remover a primária")
        void removeImage_primaryImage_promoteNextAsPrimary() {
            ProductVariant variant = buildVariant();
            ProductImage primary = mockImage(false, 1);
            ProductImage second = mockImage(false, 2);
            variant.addImage(primary, null);
            variant.addImage(second, null);

            given(primary.isPrimaryImage()).willReturn(true);

            variant.removeImage(primary);

            then(second).should().setAsPrimary();
        }

        @Test
        @DisplayName("não deve promover outra imagem quando a removida não era primária")
        void removeImage_nonPrimaryImage_doesNotPromoteOther() {
            ProductVariant variant = buildVariant();
            ProductImage primary = mockImage(false, 1);
            ProductImage second = mockImage(false, 2);
            variant.addImage(primary, null);
            variant.addImage(second, null);

            clearInvocations(primary, second);

            given(second.isPrimaryImage()).willReturn(false);

            variant.removeImage(second);

            then(primary).should(never()).setAsPrimary();
        }
    }

    // --- SET PRIMARY -------------------------------------------------------------

    @Nested
    @DisplayName("setPrimary()")
    class SetPrimary {

        @Test
        @DisplayName("deve definir imagem como primária")
        void setPrimary_nonPrimaryImage_setsAsPrimary() {
            ProductVariant variant = buildVariant();
            ProductImage image = mockImage(false, 1);
            variant.addImage(image, null);

            clearInvocations(image);

            given(image.isPrimaryImage()).willReturn(false);

            variant.setPrimary(image);

            then(image).should().setAsPrimary();
        }

        @Test
        @DisplayName("deve lançar ImageNotFoundException quando imagem não pertence à variante")
        void setPrimary_imageNotInVariant_throwsImageNotFoundException() {
            ProductVariant variant = buildVariant();
            ProductImage outsider = mockImage(false, 1);

            assertThatExceptionOfType(ImageNotFoundException.class)
                    .isThrownBy(() -> variant.setPrimary(outsider));
        }

        @Test
        @DisplayName("deve lançar ImageAlreadyPrimaryException quando imagem já é primária")
        void setPrimary_alreadyPrimary_throwsImageAlreadyPrimaryException() {
            ProductVariant variant = buildVariant();
            ProductImage image = mockImage(false, 1);
            variant.addImage(image, null);

            given(image.isPrimaryImage()).willReturn(true);

            assertThatExceptionOfType(ImageAlreadyPrimaryException.class)
                    .isThrownBy(() -> variant.setPrimary(image));
        }

        @Test
        @DisplayName("deve lançar NullPointerException quando imagem é nula")
        void setPrimary_nullImage_throwsNullPointerException() {
            ProductVariant variant = buildVariant();

            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> variant.setPrimary(null));
        }
    }

    // --- REORDER IMAGES ----------------------------------------------------------

    @Nested
    @DisplayName("reorderImages()")
    class ReorderImages {

        @Test
        @DisplayName("deve reordenar imagens conforme lista de ids fornecida")
        void reorderImages_validIds_reordersImages() {
            ProductVariant variant = buildVariant();
            ProductImage img1 = mockImage(false, 1);
            ProductImage img2 = mockImage(false, 2);
            UUID id1 = img1.getPublicId();
            UUID id2 = img2.getPublicId();
            variant.addImage(img1, null);
            variant.addImage(img2, null);

            variant.reorderImages(List.of(id2, id1));

            then(img2).should().changeDisplayOrder(1);
            then(img1).should().changeDisplayOrder(2);
        }

        @Test
        @DisplayName("deve lançar InvalidImageReorderException quando lista é vazia")
        void reorderImages_emptyList_throwsInvalidImageReorderException() {
            ProductVariant variant = buildVariant();

            assertThatExceptionOfType(InvalidImageReorderException.class)
                    .isThrownBy(() -> variant.reorderImages(List.of()));
        }

        @Test
        @DisplayName("deve lançar InvalidImageReorderException quando lista tem ids duplicados")
        void reorderImages_duplicateIds_throwsInvalidImageReorderException() {
            ProductVariant variant = buildVariant();
            ProductImage image = mockImage(false, 1);
            variant.addImage(image, null);
            UUID id = image.getPublicId();

            assertThatExceptionOfType(InvalidImageReorderException.class)
                    .isThrownBy(() -> variant.reorderImages(List.of(id, id)));
        }

        @Test
        @DisplayName("deve lançar InvalidImageReorderException quando tamanho da lista difere do total de imagens")
        void reorderImages_sizeMismatch_throwsInvalidImageReorderException() {
            ProductVariant variant = buildVariant();
            ProductImage image = mockImage(false, 1);
            variant.addImage(image, null);

            assertThatExceptionOfType(InvalidImageReorderException.class)
                    .isThrownBy(() -> variant.reorderImages(List.of(UUID.randomUUID(), UUID.randomUUID())));
        }

        @Test
        @DisplayName("deve lançar NullPointerException quando lista é nula")
        void reorderImages_nullList_throwsNullPointerException() {
            ProductVariant variant = buildVariant();

            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> variant.reorderImages(null));
        }
    }

    // --- CHANGE COLOR / SIZE / PRICE --------------------------------------------

    @Nested
    @DisplayName("changeColor()")
    class ChangeColor {

        @Test
        @DisplayName("deve alterar a cor da variante")
        void changeColor_newColor_changesColor() {
            ProductVariant variant = buildVariant();
            Color newColor = new Color("Vermelho");

            variant.changeColor(newColor);

            assertThat(variant.getColor()).isEqualTo(newColor);
        }

        @Test
        @DisplayName("deve lançar UnchangedValueException quando nova cor é igual à atual")
        void changeColor_sameColor_throwsUnchangedValueException() {
            ProductVariant variant = buildVariant();

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> variant.changeColor(COLOR));
        }
    }

    @Nested
    @DisplayName("changeSize()")
    class ChangeSize {

        @Test
        @DisplayName("deve alterar o tamanho da variante")
        void changeSize_newSize_changesSize() {
            ProductVariant variant = buildVariant();
            Size newSize = new Size("G");

            variant.changeSize(newSize);

            assertThat(variant.getSize()).isEqualTo(newSize);
        }

        @Test
        @DisplayName("deve lançar UnchangedValueException quando novo tamanho é igual ao atual")
        void changeSize_sameSize_throwsUnchangedValueException() {
            ProductVariant variant = buildVariant();

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> variant.changeSize(SIZE));
        }
    }

    @Nested
    @DisplayName("changePrice()")
    class ChangePrice {

        @Test
        @DisplayName("deve alterar o preço da variante")
        void changePrice_newPrice_changesPrice() {
            ProductVariant variant = buildVariant();
            Money newPrice = new Money(new BigDecimal("149.90"));

            variant.changePrice(newPrice);

            assertThat(variant.getPrice()).isEqualTo(newPrice);
        }

        @Test
        @DisplayName("deve lançar UnchangedValueException quando novo preço é igual ao atual")
        void changePrice_samePrice_throwsUnchangedValueException() {
            ProductVariant variant = buildVariant();

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> variant.changePrice(PRICE));
        }
    }

    // --- STOCK -------------------------------------------------------------------

    @Nested
    @DisplayName("stock()")
    class Stock {

        @Test
        @DisplayName("deve aumentar o estoque corretamente")
        void increaseStock_validQuantity_increasesStock() {
            ProductVariant variant = buildVariant();

            variant.increaseStock(new StockQuantity(5));

            assertThat(variant.getStockQuantity().value()).isEqualTo(15);
        }

        @Test
        @DisplayName("deve diminuir o estoque corretamente")
        void decreaseStock_validQuantity_decreasesStock() {
            ProductVariant variant = buildVariant();

            variant.decreaseStock(new StockQuantity(3));

            assertThat(variant.getStockQuantity().value()).isEqualTo(7);
        }

        @Test
        @DisplayName("deve lançar InsufficientStockException quando estoque insuficiente")
        void decreaseStock_insufficientStock_throwsInsufficientStockException() {
            ProductVariant variant = buildVariant();

            assertThatExceptionOfType(InsufficientStockException.class)
                    .isThrownBy(() -> variant.decreaseStock(new StockQuantity(99)));
        }

        @Test
        @DisplayName("hasStock deve retornar true quando estoque é suficiente")
        void hasStock_sufficientStock_returnsTrue() {
            ProductVariant variant = buildVariant();

            assertThat(variant.hasStock(10)).isTrue();
        }

        @Test
        @DisplayName("hasStock deve retornar false quando estoque é insuficiente")
        void hasStock_insufficientStock_returnsFalse() {
            ProductVariant variant = buildVariant();

            assertThat(variant.hasStock(11)).isFalse();
        }

        @Test
        @DisplayName("deve permitir zerar o estoque")
        void decreaseStock_toZero_setsStockToZero() {
            ProductVariant variant = buildVariant();

            variant.decreaseStock(new StockQuantity(10));

            assertThat(variant.getStockQuantity().value()).isZero();
        }
    }

    // --- ACTIVATE / DEACTIVATE ---------------------------------------------------

    @Nested
    @DisplayName("activate() e deactivate()")
    class ActivateDeactivate {

        @Test
        @DisplayName("deve desativar variante ativa")
        void deactivate_activeVariant_deactivates() {
            ProductVariant variant = buildVariant();

            variant.deactivate();

            assertThat(variant.isActive()).isFalse();
        }

        @Test
        @DisplayName("deve lançar VariantAlreadyDisableException ao desativar variante já inativa")
        void deactivate_alreadyInactive_throwsVariantAlreadyDisableException() {
            ProductVariant variant = buildVariant();
            variant.deactivate();

            assertThatExceptionOfType(VariantAlreadyDisableException.class)
                    .isThrownBy(variant::deactivate);
        }

        @Test
        @DisplayName("deve ativar variante inativa")
        void activate_inactiveVariant_activates() {
            ProductVariant variant = buildVariant();
            variant.deactivate();

            variant.activate();

            assertThat(variant.isActive()).isTrue();
        }

        @Test
        @DisplayName("deve lançar VariantAlreadyActiveException ao ativar variante já ativa")
        void activate_alreadyActive_throwsVariantAlreadyActiveException() {
            ProductVariant variant = buildVariant();

            assertThatExceptionOfType(VariantAlreadyActiveException.class)
                    .isThrownBy(variant::activate);
        }
    }
}