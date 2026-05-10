package com.javacore.spring_api_luvine.user.domain.valueObject;

import com.javacore.spring_api_luvine.user.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Email")
class EmailTest {

    // --- CRIAÇÃO VÁLIDA --------------------------------------------------

    @Nested
    @DisplayName("creation()")
    class Creation {

        @Test
        @DisplayName("deve criar Email com endereço válido")
        void shouldCreateEmailWithValidAddress() {
            Email email = new Email("user@example.com");
            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("deve criar Email com subdomínio")
        void shouldCreateEmailWithSubdomain() {
            Email email = new Email("user@mail.example.com");
            assertThat(email.value()).isEqualTo("user@mail.example.com");
        }

        @Test
        @DisplayName("deve criar Email com caracteres especiais permitidos antes do @")
        void shouldCreateEmailWithSpecialCharsBeforeAt() {
            Email email = new Email("user.name+tag@example.com");
            assertThat(email.value()).isEqualTo("user.name+tag@example.com");
        }

        @Test
        @DisplayName("deve criar Email com números no endereço")
        void shouldCreateEmailWithNumbers() {
            Email email = new Email("user123@example456.com");
            assertThat(email.value()).isEqualTo("user123@example456.com");
        }
    }

    // --- NORMALIZAÇÃO --------------------------------------------------

    @Nested
    @DisplayName("normalization()")
    class Normalization {

        @Test
        @DisplayName("deve converter email para letras minúsculas")
        void shouldNormalizeToLowerCase() {
            Email email = new Email("User@Example.COM");
            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("deve remover espaços no início e no fim")
        void shouldTrimLeadingAndTrailingWhitespace() {
            Email email = new Email("   user@example.com   ");
            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("deve normalizar simultaneamente maiúsculas e espaços")
        void shouldNormalizeUpperCaseAndWhitespaceTogether() {
            Email email = new Email("  USER@EXAMPLE.COM  ");
            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("valor armazenado nunca deve conter espaços nas bordas")
        void storedValueShouldNeverHaveBorderSpaces() {
            Email email = new Email(" test@test.com ");
            assertThat(email.value())
                    .doesNotStartWith(" ")
                    .doesNotEndWith(" ");
        }

        @Test
        @DisplayName("valor armazenado nunca deve conter letras maiúsculas")
        void storedValueShouldNeverHaveUpperCaseLetters() {
            Email email = new Email("UPPER@DOMAIN.COM");
            assertThat(email.value()).isEqualTo(email.value().toLowerCase());
        }
    }

    // --- TRATAMENTO DE EXCEÇÕES --------------------------------------------------

    @Nested
    @DisplayName("exceptionHandling()")
    class ExceptionHandling {

        @Test
        @DisplayName("deve lançar InvalidEmailException quando valor for null")
        void shouldThrowWhenValueIsNull() {
            assertThatThrownBy(() -> new Email(null))
                    .isInstanceOf(InvalidEmailException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidEmailException quando valor for blank")
        void shouldThrowWhenValueIsBlank() {
            assertThatThrownBy(() -> new Email("   "))
                    .isInstanceOf(InvalidEmailException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidEmailException quando valor for string vazia")
        void shouldThrowWhenValueIsEmpty() {
            assertThatThrownBy(() -> new Email(""))
                    .isInstanceOf(InvalidEmailException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidEmailException quando não houver @")
        void shouldThrowWhenMissingAtSign() {
            assertThatThrownBy(() -> new Email("invalidemail.com"))
                    .isInstanceOf(InvalidEmailException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidEmailException quando email for apenas @")
        void shouldThrowWhenValueIsOnlyAtSign() {
            assertThatThrownBy(() -> new Email("@"))
                    .isInstanceOf(InvalidEmailException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidEmailException quando @ estiver no início")
        void shouldThrowWhenAtSignIsAtStart() {
            assertThatThrownBy(() -> new Email("@example.com"))
                    .isInstanceOf(InvalidEmailException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidEmailException quando @ estiver no fim")
        void shouldThrowWhenAtSignIsAtEnd() {
            assertThatThrownBy(() -> new Email("user@"))
                    .isInstanceOf(InvalidEmailException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidEmailException quando valor for apenas espaços após trim (sem @)")
        void shouldThrowWhenValueBecomesBlankAfterTrim() {
            assertThatThrownBy(() -> new Email("        "))
                    .isInstanceOf(InvalidEmailException.class);
        }
    }

    // --- IGUALDADE (record) --------------------------------------------------

    @Nested
    @DisplayName("equality()")
    class Equality {

        @Test
        @DisplayName("dois Emails com mesmo valor devem ser iguais")
        void shouldBeEqualWhenSameValue() {
            Email a = new Email("user@example.com");
            Email b = new Email("user@example.com");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("dois Emails normalizados para o mesmo valor devem ser iguais")
        void shouldBeEqualAfterNormalization() {
            Email a = new Email("USER@EXAMPLE.COM");
            Email b = new Email("user@example.com");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("dois Emails com valores diferentes devem ser diferentes")
        void shouldNotBeEqualWhenDifferentValues() {
            Email a = new Email("user@example.com");
            Email b = new Email("other@example.com");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("hashCode deve ser igual para Emails com mesmo valor normalizado")
        void shouldHaveSameHashCodeForEqualEmails() {
            Email a = new Email("USER@EXAMPLE.COM");
            Email b = new Email("user@example.com");
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }
}