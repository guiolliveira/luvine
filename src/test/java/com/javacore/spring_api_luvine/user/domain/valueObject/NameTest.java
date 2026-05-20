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
            PersonName name = new PersonName("João");
            assertThat(name.value()).isEqualTo("João");
        }

        @Test
        @DisplayName("deve criar Name com nome composto")
        void shouldCreateNameWithCompoundName() {
            PersonName name = new PersonName("João Silva");
            assertThat(name.value()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("deve criar Name com três ou mais palavras")
        void shouldCreateNameWithThreeOrMoreWords() {
            PersonName name = new PersonName("Maria das Graças");
            assertThat(name.value()).isEqualTo("Maria Das Graças");
        }

        @Test
        @DisplayName("deve criar Name com nome de uma única letra")
        void shouldCreateNameWithSingleLetter() {
            PersonName name = new PersonName("A");
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

        @Test
        @DisplayName("deve lançar InvalidNameException quando valor for null")
        void shouldThrowWhenValueIsNull() {
            assertThatThrownBy(() -> new PersonName(null))
                    .isInstanceOf(InvalidNameException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidNameException quando valor for string vazia")
        void shouldThrowWhenValueIsEmpty() {
            assertThatThrownBy(() -> new PersonName(""))
                    .isInstanceOf(InvalidNameException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidNameException quando valor for blank (só espaços)")
        void shouldThrowWhenValueIsBlank() {
            assertThatThrownBy(() -> new PersonName("     "))
                    .isInstanceOf(InvalidNameException.class);
        }

        @Test
        @DisplayName("deve lançar InvalidNameException quando valor for tab ou quebra de linha")
        void shouldThrowWhenValueIsTabOrNewline() {
            assertThatThrownBy(() -> new PersonName("\t"))
                    .isInstanceOf(InvalidNameException.class);
            assertThatThrownBy(() -> new PersonName("\n"))
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
            PersonName a = new PersonName("Joao Silva");
            PersonName b = new PersonName("Joao Silva");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("dois Names normalizados para o mesmo valor devem ser iguais")
        void shouldBeEqualAfterNormalization() {
            PersonName a = new PersonName("JOAO SILVA");
            PersonName b = new PersonName("joao silva");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("dois Names com valores diferentes devem ser diferentes")
        void shouldNotBeEqualWhenDifferentValues() {
            PersonName a = new PersonName("Joao Silva");
            PersonName b = new PersonName("Pedro Souza");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("hashCode deve ser igual para Names com mesmo valor normalizado")
        void shouldHaveSameHashCodeForEqualNames() {
            PersonName a = new PersonName("JOAO SILVA");
            PersonName b = new PersonName("joao silva");
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }
}