package com.javacore.spring_api_luvine.user.domain.valueObject;

import com.javacore.spring_api_luvine.user.domain.exception.InvalidNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Name")
class NameTest {

    // --- CRIAÇÃO VÁLIDA --------------------------------------------------

    @Nested
    @DisplayName("creation()")
    class Creation {

        @Test
        @DisplayName("deve criar Name com nome simples válido")
        void shouldCreateNameWithSimpleValidName() {
            Name name = new Name("João");
            assertThat(name.value()).isEqualTo("João");
        }

        @Test
        @DisplayName("deve criar Name com nome composto")
        void shouldCreateNameWithCompoundName() {
            Name name = new Name("João Silva");
            assertThat(name.value()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("deve criar Name com três ou mais palavras")
        void shouldCreateNameWithThreeOrMoreWords() {
            Name name = new Name("Maria das Graças");
            assertThat(name.value()).isEqualTo("Maria Das Graças");
        }

        @Test
        @DisplayName("deve criar Name com nome de uma única letra")
        void shouldCreateNameWithSingleLetter() {
            Name name = new Name("A");
            assertThat(name.value()).isEqualTo("A");
        }
    }

    // --- NORMALIZAÇÃO --------------------------------------------------

    @Nested
    @DisplayName("normalization()")
    class Normalization {

        @Test
        @DisplayName("deve capitalizar a primeira letra de cada palavra")
        void shouldCapitalizeFirstLetterOfEachWord() {
            Name name = new Name("joao pedro silva");
            assertThat(name.value()).isEqualTo("Joao Pedro Silva");
        }

        @Test
        @DisplayName("deve converter letras restantes para minúsculas")
        void shouldLowerCaseRemainingLetters() {
            Name name = new Name("JOAO SILVA");
            assertThat(name.value()).isEqualTo("Joao Silva");
        }

        @Test
        @DisplayName("deve remover espaços extras entre palavras")
        void shouldCollapseMultipleSpacesBetweenWords() {
            Name name = new Name("joao   pedro");
            assertThat(name.value()).isEqualTo("Joao Pedro");
        }

        @Test
        @DisplayName("deve remover espaços no início e no fim")
        void shouldTrimLeadingAndTrailingWhitespace() {
            Name name = new Name("   joao silva   ");
            assertThat(name.value()).isEqualTo("Joao Silva");
        }

        @Test
        @DisplayName("deve normalizar simultaneamente maiúsculas, espaços extras e bordas")
        void shouldNormalizeUpperCaseAndWhitespaceTogether() {
            Name name = new Name("  JOAO   PEDRO SILVA  ");
            assertThat(name.value()).isEqualTo("Joao Pedro Silva");
        }

        @Test
        @DisplayName("valor armazenado nunca deve conter espaços nas bordas")
        void storedValueShouldNeverHaveBorderSpaces() {
            Name name = new Name("  joao  ");
            assertThat(name.value())
                    .doesNotStartWith(" ")
                    .doesNotEndWith(" ");
        }

        @Test
        @DisplayName("valor armazenado nunca deve conter espaços duplos entre palavras")
        void storedValueShouldNeverHaveDoubleSpaces() {
            Name name = new Name("joao    silva");
            assertThat(name.value()).doesNotContain("  ");
        }
    }

    // --- TRATAMENTO DE EXCEÇÕES --------------------------------------------------

    @Nested
    @DisplayName("exceptionHandling()")
    class ExceptionHandling {

        @Test
        @DisplayName("deve lançar InvalidNameException quando valor for null")
        void shouldThrowWhenValueIsNull() {
            assertThatThrownBy(() -> new Name(null))
                    .isInstanceOf(InvalidNameException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidNameException quando valor for string vazia")
        void shouldThrowWhenValueIsEmpty() {
            assertThatThrownBy(() -> new Name(""))
                    .isInstanceOf(InvalidNameException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidNameException quando valor for blank (só espaços)")
        void shouldThrowWhenValueIsBlank() {
            assertThatThrownBy(() -> new Name("     "))
                    .isInstanceOf(InvalidNameException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidNameException quando valor for tab ou quebra de linha")
        void shouldThrowWhenValueIsTabOrNewline() {
            assertThatThrownBy(() -> new Name("\t"))
                    .isInstanceOf(InvalidNameException.class);
            assertThatThrownBy(() -> new Name("\n"))
                    .isInstanceOf(InvalidNameException.class);
        }
    }

    // --- IGUALDADE (record) --------------------------------------------------

    @Nested
    @DisplayName("equality()")
    class Equality {

        @Test
        @DisplayName("dois Names com mesmo valor devem ser iguais")
        void shouldBeEqualWhenSameValue() {
            Name a = new Name("Joao Silva");
            Name b = new Name("Joao Silva");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("dois Names normalizados para o mesmo valor devem ser iguais")
        void shouldBeEqualAfterNormalization() {
            Name a = new Name("JOAO SILVA");
            Name b = new Name("joao silva");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("dois Names com valores diferentes devem ser diferentes")
        void shouldNotBeEqualWhenDifferentValues() {
            Name a = new Name("Joao Silva");
            Name b = new Name("Pedro Souza");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("hashCode deve ser igual para Names com mesmo valor normalizado")
        void shouldHaveSameHashCodeForEqualNames() {
            Name a = new Name("JOAO SILVA");
            Name b = new Name("joao silva");
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }
}