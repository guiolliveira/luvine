package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidDescriptionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DisplayName("Description")
class DescriptionTest {

    // --- CONSTRUTOR --------------------------------------------------------------

    @Nested
    @DisplayName("construtor")
    class Constructor {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("deve lançar InvalidDescriptionException quando valor é nulo ou vazio")
        void constructor_nullOrEmpty_throwsInvalidDescriptionException(String value) {
            assertThatExceptionOfType(InvalidDescriptionException.class)
                    .isThrownBy(() -> new Description(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("deve lançar InvalidDescriptionException quando valor é apenas espaços em branco")
        void constructor_blankValue_throwsInvalidDescriptionException(String value) {
            assertThatExceptionOfType(InvalidDescriptionException.class)
                    .isThrownBy(() -> new Description(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {"ab", "x", "  a  "})
        @DisplayName("deve lançar InvalidDescriptionException quando valor tem menos de 3 caracteres após normalização")
        void constructor_tooShort_throwsInvalidDescriptionException(String value) {
            assertThatExceptionOfType(InvalidDescriptionException.class)
                    .isThrownBy(() -> new Description(value));
        }

        @Test
        @DisplayName("deve aceitar valor com exatamente 3 caracteres")
        void constructor_minLength_doesNotThrow() {
            Description description = new Description("abc");
            assertThat(description.value()).isEqualTo("abc");
        }

        @Test
        @DisplayName("deve aceitar descrição longa sem limite máximo")
        void constructor_longDescription_doesNotThrow() {
            String longText = "a".repeat(1000);
            Description description = new Description(longText);
            assertThat(description.value()).isEqualTo(longText);
        }

        @Test
        @DisplayName("deve aceitar descrição normal com pontuação e números")
        void constructor_validDescription_doesNotThrow() {
            Description description = new Description("Camiseta polo masculina tamanho M, cor azul.");
            assertThat(description.value()).isEqualTo("Camiseta polo masculina tamanho M, cor azul.");
        }
    }

    // --- NORMALIZAÇÃO ------------------------------------------------------------

    @Nested
    @DisplayName("normalization()")
    class Normalization {

        @Test
        @DisplayName("deve remover espaços nas bordas")
        void constructor_surroundingSpaces_trimsValue() {
            Description description = new Description("  Descrição do produto  ");
            assertThat(description.value()).isEqualTo("Descrição do produto");
        }

        @Test
        @DisplayName("deve colapsar espaços múltiplos entre palavras")
        void constructor_multipleSpaces_collapsesToSingle() {
            Description description = new Description("Descrição   do   produto");
            assertThat(description.value()).isEqualTo("Descrição do produto");
        }

        @Test
        @DisplayName("valor armazenado nunca deve conter espaços nas bordas")
        void constructor_anyInput_neverHasBorderSpaces() {
            Description description = new Description("  texto válido aqui  ");
            assertThat(description.value())
                    .doesNotStartWith(" ")
                    .doesNotEndWith(" ");
        }

        @Test
        @DisplayName("valor armazenado nunca deve conter espaços duplos")
        void constructor_anyInput_neverHasDoubleSpaces() {
            Description description = new Description("texto    com    espaços");
            assertThat(description.value()).doesNotContain("  ");
        }
    }

    // --- EQUALS / HASHCODE -------------------------------------------------------

    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsAndHashCode {

        @Test
        @DisplayName("deve ser igual a outra Description com o mesmo valor")
        void equals_sameValue_returnsTrue() {
            Description a = new Description("Descrição do produto");
            Description b = new Description("Descrição do produto");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("deve ser diferente de outra Description com valor distinto")
        void equals_differentValue_returnsFalse() {
            Description a = new Description("Descrição do produto");
            Description b = new Description("Outra descrição aqui");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("hashCode deve ser igual para Descriptions com o mesmo valor")
        void hashCode_sameValue_returnsSameHash() {
            Description a = new Description("Descrição do produto");
            Description b = new Description("Descrição do produto");
            assertThat(a).hasSameHashCodeAs(b);
        }
    }
}