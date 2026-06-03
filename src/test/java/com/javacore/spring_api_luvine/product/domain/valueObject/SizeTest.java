package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidSizeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DisplayName("Size")
class SizeTest {

    // --- CRIAÇÃO VÁLIDA -------------------------------------------------------------

    @Nested
    @DisplayName("valores válidos")
    class ValidValues {

        @ParameterizedTest
        @ValueSource(strings = {"PP", "P", "M", "G", "GG", "XG", "XGG"})
        @DisplayName("deve aceitar todos os tamanhos permitidos")
        void shouldAcceptAllValidSizes(String size) {
            assertThat(new Size(size).value()).isEqualTo(size);
        }

        @ParameterizedTest
        @ValueSource(strings = {"pp", "p", "m", "g", "gg", "xg", "xgg"})
        @DisplayName("deve normalizar para uppercase")
        void shouldNormalizeToUpperCase(String size) {
            assertThat(new Size(size).value()).isEqualTo(size.toUpperCase());
        }

        @Test
        @DisplayName("deve remover espaços antes de normalizar")
        void shouldTrimBeforeNormalizing() {
            assertThat(new Size("  m  ").value()).isEqualTo("M");
        }

        @Test
        @DisplayName("dois Sizes com mesmo valor devem ser iguais")
        void shouldBeEqualWhenSameValue() {
            assertThat(new Size("M")).isEqualTo(new Size("m"));
        }

        @Test
        @DisplayName("dois Sizes com valores diferentes não devem ser iguais")
        void shouldNotBeEqualWhenDifferentValues() {
            assertThat(new Size("M")).isNotEqualTo(new Size("G"));
        }
    }

    // --- CRIAÇÃO INVÁLIDA -------------------------------------------------------------

    @Nested
    @DisplayName("valores inválidos")
    class InvalidValues {

        @Test
        @DisplayName("deve lançar InvalidSizeException quando null")
        void shouldThrowWhenNull() {
            assertThatExceptionOfType(InvalidSizeException.class)
                    .isThrownBy(() -> new Size(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "  "})
        @DisplayName("deve lançar InvalidSizeException quando blank ou vazio")
        void shouldThrowWhenBlankOrEmpty(String value) {
            assertThatExceptionOfType(InvalidSizeException.class)
                    .isThrownBy(() -> new Size(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {"XXL", "XL", "XXXG", "S", "L", "2XG", "EXTRA"})
        @DisplayName("deve lançar InvalidSizeException para tamanhos não permitidos")
        void shouldThrowForDisallowedSizes(String size) {
            assertThatExceptionOfType(InvalidSizeException.class)
                    .isThrownBy(() -> new Size(size));
        }
    }
}