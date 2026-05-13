package com.javacore.spring_api_luvine.user.domain.valueObject;

import com.javacore.spring_api_luvine.user.domain.exception.InvalidCepException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

@DisplayName("Cep")
class CepTest {

    // --- FIXTURE -------------------------------------------------------------

    private static final String VALID_CEP_DIGITS = "01310100";
    private static final String VALID_CEP_FORMATTED = "01310-100";
    private static final String VALID_CEP_WITH_DOTS = "01.310-100";

    // --- CEP() - CONSTRUCTOR -------------------------------------------------

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("deve aceitar CEP com 8 dígitos sem formatação")
        void shouldAcceptEightDigitCep() {
            assertThatNoException()
                    .isThrownBy(() -> new Cep(VALID_CEP_DIGITS));
        }

        @Test
        @DisplayName("deve aceitar CEP no formato XXXXX-XXX")
        void shouldAcceptFormattedCep() {
            assertThatNoException()
                    .isThrownBy(() -> new Cep(VALID_CEP_FORMATTED));
        }

        @Test
        @DisplayName("deve aceitar CEP com pontos e traços")
        void shouldAcceptCepWithDotsAndDashes() {
            assertThatNoException()
                    .isThrownBy(() -> new Cep(VALID_CEP_WITH_DOTS));
        }

        @Test
        @DisplayName("deve normalizar CEP removendo caracteres não numéricos")
        void shouldNormalizeCepRemovingNonDigits() {
            Cep cep = new Cep(VALID_CEP_FORMATTED);

            assertThat(cep.value()).isEqualTo(VALID_CEP_DIGITS);
        }

        @Test
        @DisplayName("deve normalizar CEP com espaços e traços")
        void shouldNormalizeCepWithSpacesAndDashes() {
            Cep cep = new Cep("01310 100");

            assertThat(cep.value()).isEqualTo(VALID_CEP_DIGITS);
        }

        @Test
        @DisplayName("deve lançar InvalidCepException quando valor é nulo")
        void shouldThrowWhenNull() {
            assertThatExceptionOfType(InvalidCepException.class)
                    .isThrownBy(() -> new Cep(null));
        }

        @Test
        @DisplayName("deve lançar InvalidCepException quando valor está em branco")
        void shouldThrowWhenBlank() {
            assertThatExceptionOfType(InvalidCepException.class)
                    .isThrownBy(() -> new Cep("   "));
        }

        @Test
        @DisplayName("deve lançar InvalidCepException quando valor está vazio")
        void shouldThrowWhenEmpty() {
            assertThatExceptionOfType(InvalidCepException.class)
                    .isThrownBy(() -> new Cep(""));
        }

        @ParameterizedTest(name = "CEP inválido: \"{0}\"")
        @ValueSource(strings = {
                "0131010",     // 7 dígitos
                "013101000",   // 9 dígitos
                "ABCDEFGH",    // somente letras
                "0131010A",    // letra no final
                "A1310100",    // letra no início
        })
        @DisplayName("deve lançar InvalidCepException para CEPs com formato inválido")
        void shouldThrowForInvalidFormats(String input) {
            assertThatExceptionOfType(InvalidCepException.class)
                    .isThrownBy(() -> new Cep(input));
        }
    }

    // --- GETFORMATTED() ------------------------------------------------------

    @Nested
    @DisplayName("getFormatted()")
    class GetFormatted {

        @Test
        @DisplayName("deve retornar CEP no formato XXXXX-XXX")
        void shouldReturnFormattedCep() {
            Cep cep = new Cep(VALID_CEP_DIGITS);

            assertThat(cep.getFormatted()).isEqualTo(VALID_CEP_FORMATTED);
        }

        @Test
        @DisplayName("deve retornar o mesmo formato independente do input (com ou sem traço)")
        void shouldReturnSameFormatRegardlessOfInput() {
            Cep fromDigits    = new Cep(VALID_CEP_DIGITS);
            Cep fromFormatted = new Cep(VALID_CEP_FORMATTED);

            assertThat(fromDigits.getFormatted()).isEqualTo(fromFormatted.getFormatted());
        }

        @Test
        @DisplayName("deve sempre ter exatamente 9 caracteres (XXXXX-XXX)")
        void shouldAlwaysHaveNineCharacters() {
            Cep cep = new Cep(VALID_CEP_DIGITS);

            assertThat(cep.getFormatted()).hasSize(9);
        }

        @Test
        @DisplayName("deve conter exatamente um traço na posição correta")
        void shouldContainDashAtCorrectPosition() {
            Cep cep = new Cep(VALID_CEP_DIGITS);
            String formatted = cep.getFormatted();

            assertThat(formatted.charAt(5)).isEqualTo('-');
            assertThat(formatted.replace("-", "")).isEqualTo(VALID_CEP_DIGITS);
        }
    }

    // --- EQUALS E HASHCODE ---------------------------------------------------

    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsAndHashCode {

        @Test
        @DisplayName("dois Ceps com mesmo valor normalizado devem ser iguais")
        void shouldBeEqualWhenSameNormalizedValue() {
            Cep fromDigits    = new Cep(VALID_CEP_DIGITS);
            Cep fromFormatted = new Cep(VALID_CEP_FORMATTED);

            assertThat(fromDigits).isEqualTo(fromFormatted);
        }

        @Test
        @DisplayName("dois Ceps com valores diferentes não devem ser iguais")
        void shouldNotBeEqualWhenDifferentValues() {
            Cep cep1 = new Cep("01310100");
            Cep cep2 = new Cep("04538133");

            assertThat(cep1).isNotEqualTo(cep2);
        }

        @Test
        @DisplayName("hashCode deve ser igual para Ceps com mesmo valor normalizado")
        void shouldHaveSameHashCodeForEqualCeps() {
            Cep fromDigits    = new Cep(VALID_CEP_DIGITS);
            Cep fromFormatted = new Cep(VALID_CEP_FORMATTED);

            assertThat(fromDigits.hashCode()).isEqualTo(fromFormatted.hashCode());
        }
    }
}