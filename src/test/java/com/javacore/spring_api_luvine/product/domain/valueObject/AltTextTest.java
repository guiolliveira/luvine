package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidAltTextException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DisplayName("AltText")
class AltTextTest {

    // --- CONSTRUTOR --------------------------------------------------------------

    @Nested
    @DisplayName("construtor")
    class Constructor {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("deve lançar InvalidAltTextException quando valor é nulo ou vazio")
        void constructor_nullOrEmpty_throwsInvalidAltTextException(String value) {
            assertThatExceptionOfType(InvalidAltTextException.class)
                    .isThrownBy(() -> new AltText(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("deve lançar InvalidAltTextException quando valor é apenas espaços em branco")
        void constructor_blankValue_throwsInvalidAltTextException(String value) {
            assertThatExceptionOfType(InvalidAltTextException.class)
                    .isThrownBy(() -> new AltText(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {"ab", "x", "  a  "})
        @DisplayName("deve lançar InvalidAltTextException quando valor tem menos de 3 caracteres após normalização")
        void constructor_tooShort_throwsInvalidAltTextException(String value) {
            assertThatExceptionOfType(InvalidAltTextException.class)
                    .isThrownBy(() -> new AltText(value));
        }

        @Test
        @DisplayName("deve lançar InvalidAltTextException quando valor excede 150 caracteres")
        void constructor_tooLong_throwsInvalidAltTextException() {
            String tooLong = "a".repeat(151);
            assertThatExceptionOfType(InvalidAltTextException.class)
                    .isThrownBy(() -> new AltText(tooLong));
        }

        @Test
        @DisplayName("deve aceitar valor válido com exatamente 3 caracteres")
        void constructor_minLength_doesNotThrow() {
            AltText altText = new AltText("abc");
            assertThat(altText.value()).isEqualTo("abc");
        }

        @Test
        @DisplayName("deve aceitar valor válido com exatamente 150 caracteres")
        void constructor_maxLength_doesNotThrow() {
            String maxLength = "a".repeat(150);
            AltText altText = new AltText(maxLength);
            assertThat(altText.value()).isEqualTo(maxLength);
        }

        @Test
        @DisplayName("deve aceitar texto descritivo normal")
        void constructor_validText_doesNotThrow() {
            AltText altText = new AltText("Imagem do produto em azul");
            assertThat(altText.value()).isEqualTo("Imagem do produto em azul");
        }
    }

    // --- NORMALIZAÇÃO ------------------------------------------------------------

    @Nested
    @DisplayName("normalization()")
    class Normalization {

        @Test
        @DisplayName("deve remover espaços nas bordas")
        void constructor_surroundingSpaces_trimsValue() {
            AltText altText = new AltText("  Imagem do produto  ");
            assertThat(altText.value()).isEqualTo("Imagem do produto");
        }

        @Test
        @DisplayName("deve colapsar espaços múltiplos entre palavras")
        void constructor_multipleSpaces_collapsesToSingle() {
            AltText altText = new AltText("Imagem   do   produto");
            assertThat(altText.value()).isEqualTo("Imagem do produto");
        }

        @Test
        @DisplayName("valor armazenado nunca deve conter espaços nas bordas")
        void constructor_anyInput_neverHasBorderSpaces() {
            AltText altText = new AltText("  texto válido  ");
            assertThat(altText.value())
                    .doesNotStartWith(" ")
                    .doesNotEndWith(" ");
        }

        @Test
        @DisplayName("valor armazenado nunca deve conter espaços duplos")
        void constructor_anyInput_neverHasDoubleSpaces() {
            AltText altText = new AltText("texto    com    espaços");
            assertThat(altText.value()).doesNotContain("  ");
        }
    }

    // --- EQUALS / HASHCODE -------------------------------------------------------

    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsAndHashCode {

        @Test
        @DisplayName("deve ser igual a outro AltText com o mesmo valor")
        void equals_sameValue_returnsTrue() {
            AltText a = new AltText("Imagem do produto");
            AltText b = new AltText("Imagem do produto");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("deve ser diferente de outro AltText com valor distinto")
        void equals_differentValue_returnsFalse() {
            AltText a = new AltText("Imagem do produto");
            AltText b = new AltText("Foto da variante");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("hashCode deve ser igual para AltTexts com o mesmo valor")
        void hashCode_sameValue_returnsSameHash() {
            AltText a = new AltText("Imagem do produto");
            AltText b = new AltText("Imagem do produto");
            assertThat(a).hasSameHashCodeAs(b);
        }
    }
}