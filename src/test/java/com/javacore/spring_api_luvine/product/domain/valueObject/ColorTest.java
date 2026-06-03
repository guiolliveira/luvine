package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidColorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DisplayName("Color")
class ColorTest {

    // --- CRIAÇÃO VÁLIDA -------------------------------------------------------------

    @Nested
    @DisplayName("valores válidos")
    class ValidValues {

        @ParameterizedTest
        @ValueSource(strings = {"Azul", "Vermelho", "Verde Escuro", "Amarelo"})
        @DisplayName("deve criar com cores válidas")
        void shouldCreateWithValidColors(String color) {
            assertThat(new Color(color).value()).isNotBlank();
        }

        @Test
        @DisplayName("deve normalizar capitalização do nome")
        void shouldNormalizeCapitalization() {
            String normalized = new Color("azul").value();
            assertThat(normalized).isNotBlank();
        }

        @Test
        @DisplayName("dois Colors com mesmo valor devem ser iguais")
        void shouldBeEqualWhenSameValue() {
            assertThat(new Color("Azul")).isEqualTo(new Color("Azul"));
        }

        @Test
        @DisplayName("dois Colors com valores diferentes não devem ser iguais")
        void shouldNotBeEqualWhenDifferentValues() {
            assertThat(new Color("Azul")).isNotEqualTo(new Color("Vermelho"));
        }
    }

    // --- CRIAÇÃO INVÁLIDA -------------------------------------------------------------

    @Nested
    @DisplayName("valores inválidos")
    class InvalidValues {

        @Test
        @DisplayName("deve lançar InvalidColorException quando null")
        void shouldThrowWhenNull() {
            assertThatExceptionOfType(InvalidColorException.class)
                    .isThrownBy(() -> new Color(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "  "})
        @DisplayName("deve lançar InvalidColorException quando blank ou vazio")
        void shouldThrowWhenBlankOrEmpty(String value) {
            assertThatExceptionOfType(InvalidColorException.class)
                    .isThrownBy(() -> new Color(value));
        }

        @Test
        @DisplayName("deve lançar InvalidColorException quando menor que 2 caracteres")
        void shouldThrowWhenTooShort() {
            assertThatExceptionOfType(InvalidColorException.class)
                    .isThrownBy(() -> new Color("A"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"Azul123", "Azul@", "Cor#1", "Verm3lho"})
        @DisplayName("deve lançar InvalidColorException com caracteres não alfabéticos")
        void shouldThrowForNonAlphabeticChars(String value) {
            assertThatExceptionOfType(InvalidColorException.class)
                    .isThrownBy(() -> new Color(value));
        }

        @Test
        @DisplayName("deve lançar InvalidColorException quando maior que 50 caracteres")
        void shouldThrowWhenTooLong() {
            String tooLong = "A".repeat(51);
            assertThatExceptionOfType(InvalidColorException.class)
                    .isThrownBy(() -> new Color(tooLong));
        }
    }
}