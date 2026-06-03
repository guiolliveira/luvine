package com.javacore.spring_api_luvine.user.domain.valueObject;

import com.javacore.spring_api_luvine.user.domain.exception.InvalidNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PersonName")
class PersonNameTest {

    // --- CRIAÇÃO VÁLIDA --------------------------------------------------

    @Nested
    @DisplayName("creation()")
    class Creation {

        @Test
        @DisplayName("deve criar PersonName com nome simples válido")
        void shouldCreateWithSimpleValidName() {
            PersonName name = new PersonName("João");
            assertThat(name.value()).isEqualTo("João");
        }

        @Test
        @DisplayName("deve criar PersonName com nome composto")
        void shouldCreateWithCompoundName() {
            PersonName name = new PersonName("João Silva");
            assertThat(name.value()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("deve criar PersonName com três ou mais palavras")
        void shouldCreateWithThreeOrMoreWords() {
            PersonName name = new PersonName("Maria das Graças");
            assertThat(name.value()).isEqualTo("Maria Das Graças");
        }
    }

    // --- NORMALIZAÇÃO --------------------------------------------------

    @Nested
    @DisplayName("normalization()")
    class Normalization {

        @Test
        @DisplayName("deve capitalizar a primeira letra de cada palavra")
        void shouldCapitalizeFirstLetterOfEachWord() {
            PersonName name = new PersonName("joao pedro silva");
            assertThat(name.value()).isEqualTo("Joao Pedro Silva");
        }

        @Test
        @DisplayName("deve converter letras restantes para minúsculas")
        void shouldLowerCaseRemainingLetters() {
            PersonName name = new PersonName("JOAO SILVA");
            assertThat(name.value()).isEqualTo("Joao Silva");
        }

        @Test
        @DisplayName("deve remover espaços extras entre palavras")
        void shouldCollapseMultipleSpacesBetweenWords() {
            PersonName name = new PersonName("joao   pedro");
            assertThat(name.value()).isEqualTo("Joao Pedro");
        }

        @Test
        @DisplayName("deve remover espaços no início e no fim")
        void shouldTrimLeadingAndTrailingWhitespace() {
            PersonName name = new PersonName("   joao silva   ");
            assertThat(name.value()).isEqualTo("Joao Silva");
        }

        @Test
        @DisplayName("deve normalizar simultaneamente maiúsculas, espaços extras e bordas")
        void shouldNormalizeUpperCaseAndWhitespaceTogether() {
            PersonName name = new PersonName("  JOAO   PEDRO SILVA  ");
            assertThat(name.value()).isEqualTo("Joao Pedro Silva");
        }

        @Test
        @DisplayName("valor armazenado nunca deve conter espaços nas bordas")
        void storedValueShouldNeverHaveBorderSpaces() {
            PersonName name = new PersonName("  joao  ");
            assertThat(name.value())
                    .doesNotStartWith(" ")
                    .doesNotEndWith(" ");
        }

        @Test
        @DisplayName("valor armazenado nunca deve conter espaços duplos entre palavras")
        void storedValueShouldNeverHaveDoubleSpaces() {
            PersonName name = new PersonName("joao    silva");
            assertThat(name.value()).doesNotContain("  ");
        }
    }

    // --- TRATAMENTO DE EXCEÇÕES --------------------------------------------------

    @Nested
    @DisplayName("exceptionHandling()")
    class ExceptionHandling {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("deve lançar InvalidNameException quando valor for nulo ou vazio")
        void shouldThrowWhenNullOrEmpty(String value) {
            assertThatThrownBy(() -> new PersonName(value))
                    .isInstanceOf(InvalidNameException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"     ", "\t", "\n"})
        @DisplayName("deve lançar InvalidNameException quando valor for apenas espaços em branco")
        void shouldThrowWhenBlank(String value) {
            assertThatThrownBy(() -> new PersonName(value))
                    .isInstanceOf(InvalidNameException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidNameException quando nome tiver apenas uma letra")
        void shouldThrowWhenSingleLetter() {
            assertThatThrownBy(() -> new PersonName("A"))
                    .isInstanceOf(InvalidNameException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidNameException quando nome exceder 100 caracteres")
        void shouldThrowWhenExceedsMaxLength() {
            String tooLong = "Ab".repeat(51);
            assertThatThrownBy(() -> new PersonName(tooLong))
                    .isInstanceOf(InvalidNameException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidNameException quando nome contiver números")
        void shouldThrowWhenContainsNumbers() {
            assertThatThrownBy(() -> new PersonName("Joao123"))
                    .isInstanceOf(InvalidNameException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidNameException quando nome contiver caracteres especiais")
        void shouldThrowWhenContainsSpecialChars() {
            assertThatThrownBy(() -> new PersonName("Joao@Silva"))
                    .isInstanceOf(InvalidNameException.class);
        }
    }

    // --- IGUALDADE (record) --------------------------------------------------

    @Nested
    @DisplayName("equality()")
    class Equality {

        @Test
        @DisplayName("dois PersonNames com mesmo valor devem ser iguais")
        void shouldBeEqualWhenSameValue() {
            PersonName a = new PersonName("Joao Silva");
            PersonName b = new PersonName("Joao Silva");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("dois PersonNames normalizados para o mesmo valor devem ser iguais")
        void shouldBeEqualAfterNormalization() {
            PersonName a = new PersonName("JOAO SILVA");
            PersonName b = new PersonName("joao silva");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("dois PersonNames com valores diferentes devem ser diferentes")
        void shouldNotBeEqualWhenDifferentValues() {
            PersonName a = new PersonName("Joao Silva");
            PersonName b = new PersonName("Pedro Souza");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("hashCode deve ser igual para PersonNames com mesmo valor normalizado")
        void shouldHaveSameHashCodeForEqualNames() {
            PersonName a = new PersonName("JOAO SILVA");
            PersonName b = new PersonName("joao silva");
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }
}