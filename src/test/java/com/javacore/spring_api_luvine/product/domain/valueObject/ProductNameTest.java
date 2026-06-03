package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidProductNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DisplayName("ProductName")
class ProductNameTest {

    // --- CRIAÇÃO VÁLIDA -------------------------------------------------------------

    @Nested
    @DisplayName("valores válidos")
    class ValidValues {

        @Test
        @DisplayName("deve criar com nome simples válido")
        void shouldCreateWithSimpleName() {
            assertThat(new ProductName("Camiseta Básica").value()).isEqualTo("Camiseta Básica");
        }

        @Test
        @DisplayName("deve aceitar nome com números")
        void shouldAcceptNameWithNumbers() {
            assertThat(new ProductName("Camiseta 2024").value()).isEqualTo("Camiseta 2024");
        }

        @Test
        @DisplayName("deve aceitar nome com hífen, ponto e parênteses")
        void shouldAcceptSpecialAllowedChars() {
            assertThat(new ProductName("Camiseta (Básica) - P,G").value())
                    .isEqualTo("Camiseta (Básica) - P,G");
        }

        @Test
        @DisplayName("deve normalizar espaços extras no início e fim")
        void shouldTrimLeadingAndTrailingSpaces() {
            assertThat(new ProductName("  Camiseta  ").value()).isEqualTo("Camiseta");
        }

        @Test
        @DisplayName("deve normalizar múltiplos espaços internos para um único espaço")
        void shouldNormalizeInternalSpaces() {
            assertThat(new ProductName("Camiseta   Básica").value()).isEqualTo("Camiseta Básica");
        }

        @Test
        @DisplayName("dois ProductNames com mesmo valor devem ser iguais")
        void shouldBeEqualWhenSameValue() {
            assertThat(new ProductName("Camiseta Básica")).isEqualTo(new ProductName("Camiseta Básica"));
        }

        @Test
        @DisplayName("dois ProductNames com valores diferentes não devem ser iguais")
        void shouldNotBeEqualWhenDifferentValues() {
            assertThat(new ProductName("Camiseta Básica")).isNotEqualTo(new ProductName("Calça Jeans"));
        }
    }

    // --- CRIAÇÃO INVÁLIDA -------------------------------------------------------------

    @Nested
    @DisplayName("valores inválidos")
    class InvalidValues {

        @Test
        @DisplayName("deve lançar InvalidProductNameException quando null")
        void shouldThrowWhenNull() {
            assertThatExceptionOfType(InvalidProductNameException.class)
                    .isThrownBy(() -> new ProductName(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "  "})
        @DisplayName("deve lançar InvalidProductNameException quando blank ou vazio")
        void shouldThrowWhenBlankOrEmpty(String value) {
            assertThatExceptionOfType(InvalidProductNameException.class)
                    .isThrownBy(() -> new ProductName(value));
        }

        @Test
        @DisplayName("deve lançar InvalidProductNameException quando menor que 3 caracteres")
        void shouldThrowWhenTooShort() {
            assertThatExceptionOfType(InvalidProductNameException.class)
                    .isThrownBy(() -> new ProductName("AB"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"Camiseta@", "Produto#1", "Nome!", "Test&"})
        @DisplayName("deve lançar InvalidProductNameException com caracteres especiais não permitidos")
        void shouldThrowForDisallowedSpecialChars(String value) {
            assertThatExceptionOfType(InvalidProductNameException.class)
                    .isThrownBy(() -> new ProductName(value));
        }
    }
}