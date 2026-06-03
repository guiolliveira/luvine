package com.javacore.spring_api_luvine.common.util;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnsupportedOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DisplayName("Name")
class NameUtilTest {

    // --- INSTANCIAÇÃO ------------------------------------------------------------

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("deve lançar UnsupportedOperationException ao tentar instanciar via reflexão")
        void constructor_reflectionInstantiation_throwsUnsupportedOperationException() throws Exception {
            var constructor = Name.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatExceptionOfType(java.lang.reflect.InvocationTargetException.class)
                    .isThrownBy(constructor::newInstance)
                    .withCauseInstanceOf(UnsupportedOperationException.class);
        }
    }

    // --- NORMALIZE ---------------------------------------------------------------

    @Nested
    @DisplayName("normalize()")
    class Normalize {

        @Test
        @DisplayName("deve capitalizar a primeira letra de cada palavra")
        void normalize_lowercase_capitalizesEachWord() {
            assertThat(Name.normalize("joao silva")).isEqualTo("Joao Silva");
        }

        @Test
        @DisplayName("deve converter letras restantes para minúsculas")
        void normalize_allUppercase_lowercasesRemainder() {
            assertThat(Name.normalize("JOAO SILVA")).isEqualTo("Joao Silva");
        }

        @Test
        @DisplayName("deve colapsar espaços múltiplos entre palavras")
        void normalize_multipleSpaces_collapsesToSingle() {
            assertThat(Name.normalize("joao   silva")).isEqualTo("Joao Silva");
        }

        @Test
        @DisplayName("deve remover espaços nas bordas")
        void normalize_surroundingSpaces_trimsValue() {
            assertThat(Name.normalize("  joao silva  ")).isEqualTo("Joao Silva");
        }

        @Test
        @DisplayName("deve normalizar simultaneamente maiúsculas, espaços extras e bordas")
        void normalize_mixedInput_normalizesCompletely() {
            assertThat(Name.normalize("  JOAO   SILVA  ")).isEqualTo("Joao Silva");
        }

        @Test
        @DisplayName("deve funcionar com nome de uma única palavra")
        void normalize_singleWord_capitalizesCorrectly() {
            assertThat(Name.normalize("joao")).isEqualTo("Joao");
        }
    }

    // --- IS VALID ----------------------------------------------------------------

    @Nested
    @DisplayName("isValid()")
    class IsValid {

        @Test
        @DisplayName("deve retornar true para nome válido simples")
        void isValid_validName_returnsTrue() {
            assertThat(Name.isValid("Joao")).isTrue();
        }

        @Test
        @DisplayName("deve retornar true para nome composto válido")
        void isValid_validCompoundName_returnsTrue() {
            assertThat(Name.isValid("Joao Silva")).isTrue();
        }

        @Test
        @DisplayName("deve retornar false para string vazia")
        void isValid_emptyString_returnsFalse() {
            assertThat(Name.isValid("")).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para string com apenas espaços")
        void isValid_blankString_returnsFalse() {
            assertThat(Name.isValid("   ")).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para nome com apenas uma letra")
        void isValid_singleLetter_returnsFalse() {
            assertThat(Name.isValid("A")).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para nome com mais de 100 caracteres")
        void isValid_tooLong_returnsFalse() {
            assertThat(Name.isValid("Ab".repeat(51))).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para nome com números")
        void isValid_containsNumbers_returnsFalse() {
            assertThat(Name.isValid("Joao123")).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para nome com caracteres especiais")
        void isValid_containsSpecialChars_returnsFalse() {
            assertThat(Name.isValid("Joao@Silva")).isFalse();
        }

        @Test
        @DisplayName("deve retornar true para nome com exatamente 2 caracteres")
        void isValid_minLength_returnsTrue() {
            assertThat(Name.isValid("Ab")).isTrue();
        }

        @Test
        @DisplayName("deve retornar true para nome com exatamente 100 caracteres")
        void isValid_maxLength_returnsTrue() {
            String maxLength = "A" + "b".repeat(99);
            assertThat(Name.isValid(maxLength)).isTrue();
        }
    }
}