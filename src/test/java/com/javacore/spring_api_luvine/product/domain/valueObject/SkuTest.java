package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidSkuException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DisplayName("Sku")
class SkuTest {

    // --- CRIAÇÃO VÁLIDA -------------------------------------------------------------

    @Nested
    @DisplayName("valores válidos")
    class ValidValues {

        @Test
        @DisplayName("deve criar com valor válido em uppercase")
        void shouldCreateWithValidUppercaseValue() {
            assertThat(new Sku("SKU-001").value()).isEqualTo("SKU-001");
        }

        @Test
        @DisplayName("deve normalizar para uppercase")
        void shouldNormalizeToUpperCase() {
            assertThat(new Sku("sku-001").value()).isEqualTo("SKU-001");
        }

        @Test
        @DisplayName("deve remover espaços antes de normalizar")
        void shouldTrimBeforeNormalizing() {
            assertThat(new Sku("  SKU-001  ").value()).isEqualTo("SKU-001");
        }

        @Test
        @DisplayName("deve aceitar SKU apenas com letras e números")
        void shouldAcceptAlphanumericSku() {
            assertThat(new Sku("ABC123").value()).isEqualTo("ABC123");
        }

        @Test
        @DisplayName("deve aceitar SKU com hífen")
        void shouldAcceptSkuWithHyphen() {
            assertThat(new Sku("SKU-ABC-001").value()).isEqualTo("SKU-ABC-001");
        }

        @Test
        @DisplayName("dois Skus com mesmo valor devem ser iguais")
        void shouldBeEqualWhenSameValue() {
            assertThat(new Sku("SKU-001")).isEqualTo(new Sku("sku-001"));
        }

        @Test
        @DisplayName("dois Skus com valores diferentes não devem ser iguais")
        void shouldNotBeEqualWhenDifferentValues() {
            assertThat(new Sku("SKU-001")).isNotEqualTo(new Sku("SKU-002"));
        }
    }

    // --- CRIAÇÃO INVÁLIDA -------------------------------------------------------------

    @Nested
    @DisplayName("valores inválidos")
    class InvalidValues {

        @Test
        @DisplayName("deve lançar InvalidSkuException quando null")
        void shouldThrowWhenNull() {
            assertThatExceptionOfType(InvalidSkuException.class)
                    .isThrownBy(() -> new Sku(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "  "})
        @DisplayName("deve lançar InvalidSkuException quando blank ou vazio")
        void shouldThrowWhenBlankOrEmpty(String value) {
            assertThatExceptionOfType(InvalidSkuException.class)
                    .isThrownBy(() -> new Sku(value));
        }

        @Test
        @DisplayName("deve lançar InvalidSkuException quando menor que 3 caracteres")
        void shouldThrowWhenTooShort() {
            assertThatExceptionOfType(InvalidSkuException.class)
                    .isThrownBy(() -> new Sku("AB"));
        }

        @Test
        @DisplayName("deve lançar InvalidSkuException quando maior que 50 caracteres")
        void shouldThrowWhenTooLong() {
            assertThatExceptionOfType(InvalidSkuException.class)
                    .isThrownBy(() -> new Sku("A".repeat(51)));
        }

        @ParameterizedTest
        @ValueSource(strings = {"SKU@001", "SKU#001", "SKU 001", "SKU.001"})
        @DisplayName("deve lançar InvalidSkuException com caracteres inválidos")
        void shouldThrowForInvalidCharacters(String value) {
            assertThatExceptionOfType(InvalidSkuException.class)
                    .isThrownBy(() -> new Sku(value));
        }
    }

    // --- GENERATE -------------------------------------------------------------

    @Nested
    @DisplayName("generate()")
    class Generate {

        @Test
        @DisplayName("deve retornar SKU não nulo")
        void shouldReturnNonNull() {
            assertThat(Sku.generate()).isNotNull();
        }

        @Test
        @DisplayName("deve retornar SKU com exatamente 12 caracteres")
        void shouldReturnSkuWith12Chars() {
            assertThat(Sku.generate()).hasSize(12);
        }

        @Test
        @DisplayName("deve retornar SKU apenas com letras maiúsculas e números")
        void shouldReturnUppercaseAlphanumeric() {
            assertThat(Sku.generate()).matches("^[A-Z0-9]+$");
        }

        @Test
        @DisplayName("deve gerar valores distintos a cada chamada")
        void shouldGenerateDistinctValues() {
            assertThat(Sku.generate()).isNotEqualTo(Sku.generate());
        }

        @Test
        @DisplayName("SKU gerado deve ser válido como novo Sku")
        void generatedSkuShouldBeValidForSkuCreation() {
            String generated = Sku.generate();
            assertThat(new Sku(generated).value()).isEqualTo(generated);
        }
    }
}