package com.javacore.spring_api_luvine.user.domain.valueObject;

import com.javacore.spring_api_luvine.user.domain.exception.InvalidPhoneException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

@DisplayName("Phone")
class PhoneTest {

    // --- FIXTURE -------------------------------------------------------------

    private static final String VALID_MOBILE = "11987654321"; // 11 dígitos (celular)
    private static final String VALID_LANDLINE = "1132654321"; // 10 dígitos (fixo)
    private static final String FORMATTED_MOBILE = "(11) 98765-4321";
    private static final String FORMATTED_LANDLINE = "(11) 3265-4321";

    // --- PHONE() - CONSTRUCTOR -----------------------------------------------

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("deve aceitar celular com 11 dígitos")
        void shouldAcceptMobileWithElevenDigits() {
            assertThatNoException()
                    .isThrownBy(() -> new Phone(VALID_MOBILE));
        }

        @Test
        @DisplayName("deve aceitar fixo com 10 dígitos")
        void shouldAcceptLandlineWithTenDigits() {
            assertThatNoException()
                    .isThrownBy(() -> new Phone(VALID_LANDLINE));
        }

        @Test
        @DisplayName("deve aceitar celular no formato (XX) XXXXX-XXXX")
        void shouldAcceptFormattedMobile() {
            assertThatNoException()
                    .isThrownBy(() -> new Phone(FORMATTED_MOBILE));
        }

        @Test
        @DisplayName("deve aceitar fixo no formato (XX) XXXX-XXXX")
        void shouldAcceptFormattedLandline() {
            assertThatNoException()
                    .isThrownBy(() -> new Phone(FORMATTED_LANDLINE));
        }

        @Test
        @DisplayName("deve normalizar celular removendo caracteres não numéricos")
        void shouldNormalizeMobileRemovingNonDigits() {
            Phone phone = new Phone(FORMATTED_MOBILE);

            assertThat(phone.value()).isEqualTo(VALID_MOBILE);
        }

        @Test
        @DisplayName("deve normalizar fixo removendo caracteres não numéricos")
        void shouldNormalizeLandlineRemovingNonDigits() {
            Phone phone = new Phone(FORMATTED_LANDLINE);

            assertThat(phone.value()).isEqualTo(VALID_LANDLINE);
        }

        @Test
        @DisplayName("deve lançar InvalidPhoneException quando valor é nulo")
        void shouldThrowWhenNull() {
            assertThatExceptionOfType(InvalidPhoneException.class)
                    .isThrownBy(() -> new Phone(null));
        }

        @Test
        @DisplayName("deve lançar InvalidPhoneException quando valor está em branco")
        void shouldThrowWhenBlank() {
            assertThatExceptionOfType(InvalidPhoneException.class)
                    .isThrownBy(() -> new Phone("   "));
        }

        @Test
        @DisplayName("deve lançar InvalidPhoneException quando valor está vazio")
        void shouldThrowWhenEmpty() {
            assertThatExceptionOfType(InvalidPhoneException.class)
                    .isThrownBy(() -> new Phone(""));
        }

        @ParameterizedTest(name = "telefone inválido: \"{0}\"")
        @ValueSource(strings = {
                "123456789",       // 9 dígitos — curto demais
                "119876543210",    // 12 dígitos — longo demais
                "01987654321",     // DDD começa com 0 (inválido)
                "00987654321",     // DDD 00 (inválido)
                "ABCDEFGHIJK",     // somente letras — vira string vazia após normalize
        })
        @DisplayName("deve lançar InvalidPhoneException para telefones com formato inválido")
        void shouldThrowForInvalidFormats(String input) {
            assertThatExceptionOfType(InvalidPhoneException.class)
                    .isThrownBy(() -> new Phone(input));
        }

        @Test
        @DisplayName("deve lançar InvalidPhoneException quando DDD começa com zero")
        void shouldThrowWhenDddStartsWithZero() {
            assertThatExceptionOfType(InvalidPhoneException.class)
                    .isThrownBy(() -> new Phone("01987654321"));
        }

        @Test
        @DisplayName("deve lançar InvalidPhoneException quando DDD é 00")
        void shouldThrowWhenDddIsDoubleZero() {
            assertThatExceptionOfType(InvalidPhoneException.class)
                    .isThrownBy(() -> new Phone("0032654321"));
        }
    }

    // --- GETFORMATTED() ------------------------------------------------------

    @Nested
    @DisplayName("getFormatted()")
    class GetFormatted {

        @Test
        @DisplayName("deve formatar celular (11 dígitos) como (XX) XXXXX-XXXX")
        void shouldFormatMobileWithFiveDigitSuffix() {
            Phone phone = new Phone(VALID_MOBILE);

            assertThat(phone.getFormatted()).isEqualTo(FORMATTED_MOBILE);
        }

        @Test
        @DisplayName("deve formatar fixo (10 dígitos) como (XX) XXXX-XXXX")
        void shouldFormatLandlineWithFourDigitSuffix() {
            Phone phone = new Phone(VALID_LANDLINE);

            assertThat(phone.getFormatted()).isEqualTo(FORMATTED_LANDLINE);
        }

        @Test
        @DisplayName("deve retornar o mesmo formato independente do input (com ou sem formatação)")
        void shouldReturnSameFormatRegardlessOfInput() {
            Phone fromDigits    = new Phone(VALID_MOBILE);
            Phone fromFormatted = new Phone(FORMATTED_MOBILE);

            assertThat(fromDigits.getFormatted()).isEqualTo(fromFormatted.getFormatted());
        }

        @Test
        @DisplayName("celular formatado deve ter exatamente 15 caracteres")
        void mobileFormattedShouldHaveFifteenCharacters() {
            Phone phone = new Phone(VALID_MOBILE);

            assertThat(phone.getFormatted()).hasSize(15);
        }

        @Test
        @DisplayName("fixo formatado deve ter exatamente 14 caracteres")
        void landlineFormattedShouldHaveFourteenCharacters() {
            Phone phone = new Phone(VALID_LANDLINE);

            assertThat(phone.getFormatted()).hasSize(14);
        }

        @Test
        @DisplayName("deve conter parênteses no DDD e traço no número")
        void shouldContainParenthesesAndDash() {
            Phone mobile   = new Phone(VALID_MOBILE);
            Phone landline = new Phone(VALID_LANDLINE);

            assertThat(mobile.getFormatted()).startsWith("(").contains(") ").contains("-");
            assertThat(landline.getFormatted()).startsWith("(").contains(") ").contains("-");
        }

        @Test
        @DisplayName("valor formatado removido de caracteres especiais deve igual ao value normalizado")
        void formattedStrippedShouldEqualNormalizedValue() {
            Phone mobile   = new Phone(VALID_MOBILE);
            Phone landline = new Phone(VALID_LANDLINE);

            assertThat(mobile.getFormatted().replaceAll("\\D", "")).isEqualTo(mobile.value());
            assertThat(landline.getFormatted().replaceAll("\\D", "")).isEqualTo(landline.value());
        }
    }

    // --- EQUALS E HASHCODE ---------------------------------------------------

    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsAndHashCode {

        @Test
        @DisplayName("dois Phones com mesmo valor normalizado devem ser iguais")
        void shouldBeEqualWhenSameNormalizedValue() {
            Phone fromDigits    = new Phone(VALID_MOBILE);
            Phone fromFormatted = new Phone(FORMATTED_MOBILE);

            assertThat(fromDigits).isEqualTo(fromFormatted);
        }

        @Test
        @DisplayName("celular e fixo com dígitos diferentes não devem ser iguais")
        void shouldNotBeEqualWhenDifferentValues() {
            Phone mobile   = new Phone(VALID_MOBILE);
            Phone landline = new Phone(VALID_LANDLINE);

            assertThat(mobile).isNotEqualTo(landline);
        }

        @Test
        @DisplayName("hashCode deve ser igual para Phones com mesmo valor normalizado")
        void shouldHaveSameHashCodeForEqualPhones() {
            Phone fromDigits    = new Phone(VALID_MOBILE);
            Phone fromFormatted = new Phone(FORMATTED_MOBILE);

            assertThat(fromDigits.hashCode()).isEqualTo(fromFormatted.hashCode());
        }
    }
}