package com.javacore.spring_api_luvine.shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailMask")
class EmailMaskTest {

    // --- ENTRADAS INVÁLIDAS --------------------------------------------------

    @Nested
    @DisplayName("invalidInputs()")
    class InvalidInputs {

        @Test
        @DisplayName("deve retornar 'null' quando email for null")
        void shouldReturnNullLiteralForNullEmail() {
            assertThat(EmailMask.mask(null)).isEqualTo("null");
        }

        @ParameterizedTest(name = "email em branco: \"{0}\"")
        @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
        @DisplayName("deve retornar 'null' quando email for blank")
        void shouldReturnNullLiteralForBlankEmail(String blank) {
            assertThat(EmailMask.mask(blank)).isEqualTo("null");
        }

        @Test
        @DisplayName("deve retornar 'null' quando email não contiver '@'")
        void shouldReturnNullLiteralWhenNoAtSign() {
            assertThat(EmailMask.mask("invalidemail.com")).isEqualTo("null");
        }
    }

    // --- MASCARAMENTO DE EMAIL (PARTE LOCAL) --------------------------------------------------

    @Nested
    @DisplayName("localPartMasking()")
    class LocalPartMasking {

        @Test
        @DisplayName("deve manter 1 caractere visível quando nome tem 1 caractere (visibleCount mínimo = 1)")
        void shouldShowOneCharWhenNameHasOneChar() {
            String result = EmailMask.mask("a@example.com");
            assertThat(result).startsWith("a");
            assertThat(result).doesNotContain("*");
        }

        @Test
        @DisplayName("deve manter 1 caractere visível quando nome tem 2 caracteres")
        void shouldShowOneCharWhenNameHasTwoChars() {
            String result = EmailMask.mask("ab@example.com");
            assertThat(result).startsWith("a*@");
        }

        @Test
        @DisplayName("deve manter 1 caractere visível quando nome tem 3 caracteres")
        void shouldShowOneCharWhenNameHasThreeChars() {
            String result = EmailMask.mask("abc@example.com");
            assertThat(result).startsWith("a**@");
        }

        @Test
        @DisplayName("deve manter 2 caracteres visíveis quando nome tem 6 caracteres")
        void shouldShowTwoCharsWhenNameHasSixChars() {
            String result = EmailMask.mask("user@example.com");
            assertThat(result).startsWith("u***@");
        }

        @Test
        @DisplayName("deve manter 3 caracteres visíveis quando nome tem 9 caracteres")
        void shouldShowThreeCharsWhenNameHasNineChars() {
            String result = EmailMask.mask("abcdefghi@example.com");
            assertThat(result).startsWith("abc******@");
        }

        @Test
        @DisplayName("a quantidade de asteriscos deve ser name.length - visibleCount")
        void shouldMaskRemainingCharsWithAsterisks() {
            String result = EmailMask.mask("user@example.com");
            String localPart = result.substring(0, result.indexOf('@'));

            long asterisks = localPart.chars().filter(c -> c == '*').count();
            assertThat(asterisks).isEqualTo(3);
        }
    }

    // --- PRESERVAÇÃO DE DOMINIO --------------------------------------------------

    @Nested
    @DisplayName("domainPreservation()")
    class DomainPreservation {

        @Test
        @DisplayName("deve preservar o domínio integralmente após o '@'")
        void shouldPreserveDomainIntact() {
            String result = EmailMask.mask("user@example.com");
            assertThat(result).endsWith("@example.com");
        }

        @Test
        @DisplayName("deve preservar domínio com subdomínio")
        void shouldPreserveDomainWithSubdomain() {
            String result = EmailMask.mask("user@mail.company.org");
            assertThat(result).endsWith("@mail.company.org");
        }

        @Test
        @DisplayName("deve preservar o '@' no resultado")
        void shouldAlwaysContainAtSign() {
            String result = EmailMask.mask("user@domain.io");
            assertThat(result).contains("@");
        }
    }

    // --- FORMATO GERAL DO RESULTADO --------------------------------------------------

    @Nested
    @DisplayName("generalFormat()")
    class GeneralFormat {

        @Test
        @DisplayName("resultado deve seguir o formato: visível + asteriscos + @ + domínio")
        void shouldFollowExpectedFormat() {
            assertThat(EmailMask.mask("user@example.com")).isEqualTo("u***@example.com");
        }

        @Test
        @DisplayName("deve funcionar com email que tem tag (sinal de +)")
        void shouldHandleEmailWithPlusTag() {
            String result = EmailMask.mask("user+tag@example.com");
            assertThat(result).endsWith("@example.com");
            assertThat(result).contains("*");
        }

        @Test
        @DisplayName("deve funcionar com nome muito longo (30 chars)")
        void shouldHandleLongLocalPart() {
            String longName = "a".repeat(30);
            String result = EmailMask.mask(longName + "@example.com");
            String localPart = result.substring(0, result.indexOf('@'));

            long visible = localPart.chars().filter(c -> c == 'a').count();
            long masked  = localPart.chars().filter(c -> c == '*').count();

            assertThat(visible).isEqualTo(10);
            assertThat(masked).isEqualTo(20);
        }

        @Test
        @DisplayName("resultado nunca deve ser null")
        void resultShouldNeverBeNull() {
            assertThat(EmailMask.mask(null)).isNotNull();
            assertThat(EmailMask.mask("")).isNotNull();
            assertThat(EmailMask.mask("valid@email.com")).isNotNull();
        }

        @Test
        @DisplayName("resultado nunca deve expor o email original completo")
        void shouldNeverExposeFullEmail() {
            String email = "user@example.com";
            String result = EmailMask.mask(email);
            assertThat(result).isNotEqualTo(email);
            assertThat(result).contains("*");
        }
    }
}