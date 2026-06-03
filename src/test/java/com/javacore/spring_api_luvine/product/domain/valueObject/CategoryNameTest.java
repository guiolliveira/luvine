package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidCategoryName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DisplayName("CategoryName")
class CategoryNameTest {

    // --- CONSTRUTOR --------------------------------------------------------------

    @Nested
    @DisplayName("construtor")
    class Constructor {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("deve lançar InvalidCategoryName quando valor é nulo ou vazio")
        void constructor_nullOrEmpty_throwsInvalidCategoryName(String value) {
            assertThatExceptionOfType(InvalidCategoryName.class)
                    .isThrownBy(() -> new CategoryName(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("deve lançar InvalidCategoryName quando valor é apenas espaços em branco")
        void constructor_blankValue_throwsInvalidCategoryName(String value) {
            assertThatExceptionOfType(InvalidCategoryName.class)
                    .isThrownBy(() -> new CategoryName(value));
        }

        @Test
        @DisplayName("deve lançar InvalidCategoryName quando nome tem apenas uma letra")
        void constructor_singleLetter_throwsInvalidCategoryName() {
            assertThatExceptionOfType(InvalidCategoryName.class)
                    .isThrownBy(() -> new CategoryName("A"));
        }

        @Test
        @DisplayName("deve lançar InvalidCategoryName quando nome excede 100 caracteres")
        void constructor_tooLong_throwsInvalidCategoryName() {
            String tooLong = "Ab".repeat(51);
            assertThatExceptionOfType(InvalidCategoryName.class)
                    .isThrownBy(() -> new CategoryName(tooLong));
        }

        @Test
        @DisplayName("deve lançar InvalidCategoryName quando nome contém números")
        void constructor_containsNumbers_throwsInvalidCategoryName() {
            assertThatExceptionOfType(InvalidCategoryName.class)
                    .isThrownBy(() -> new CategoryName("Categoria123"));
        }

        @Test
        @DisplayName("deve lançar InvalidCategoryName quando nome contém caracteres especiais")
        void constructor_containsSpecialChars_throwsInvalidCategoryName() {
            assertThatExceptionOfType(InvalidCategoryName.class)
                    .isThrownBy(() -> new CategoryName("Camisas@Polo"));
        }

        @Test
        @DisplayName("deve aceitar nome simples válido")
        void constructor_validSimpleName_doesNotThrow() {
            CategoryName name = new CategoryName("Camisas");
            assertThat(name.value()).isEqualTo("Camisas");
        }

        @Test
        @DisplayName("deve aceitar nome composto válido")
        void constructor_validCompoundName_doesNotThrow() {
            CategoryName name = new CategoryName("Camisas Polo");
            assertThat(name.value()).isEqualTo("Camisas Polo");
        }
    }

    // --- NORMALIZAÇÃO ------------------------------------------------------------

    @Nested
    @DisplayName("normalization()")
    class Normalization {

        @Test
        @DisplayName("deve capitalizar a primeira letra de cada palavra")
        void constructor_lowercase_capitalizesEachWord() {
            CategoryName name = new CategoryName("camisas polo");
            assertThat(name.value()).isEqualTo("Camisas Polo");
        }

        @Test
        @DisplayName("deve converter letras restantes para minúsculas")
        void constructor_allUppercase_lowercasesRemainder() {
            CategoryName name = new CategoryName("CAMISAS POLO");
            assertThat(name.value()).isEqualTo("Camisas Polo");
        }

        @Test
        @DisplayName("deve remover espaços nas bordas")
        void constructor_surroundingSpaces_trimsValue() {
            CategoryName name = new CategoryName("  Camisas  ");
            assertThat(name.value()).isEqualTo("Camisas");
        }

        @Test
        @DisplayName("deve colapsar espaços múltiplos entre palavras")
        void constructor_multipleSpaces_collapsesToSingle() {
            CategoryName name = new CategoryName("Camisas   Polo");
            assertThat(name.value()).isEqualTo("Camisas Polo");
        }
    }

    // --- EQUALS / HASHCODE -------------------------------------------------------

    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsAndHashCode {

        @Test
        @DisplayName("deve ser igual a outro CategoryName com o mesmo valor")
        void equals_sameValue_returnsTrue() {
            CategoryName a = new CategoryName("Camisas");
            CategoryName b = new CategoryName("Camisas");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("dois CategoryNames normalizados para o mesmo valor devem ser iguais")
        void equals_sameValueAfterNormalization_returnsTrue() {
            CategoryName a = new CategoryName("CAMISAS");
            CategoryName b = new CategoryName("camisas");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("deve ser diferente de outro CategoryName com valor distinto")
        void equals_differentValue_returnsFalse() {
            CategoryName a = new CategoryName("Camisas");
            CategoryName b = new CategoryName("Calças");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("hashCode deve ser igual para CategoryNames com mesmo valor normalizado")
        void hashCode_sameNormalizedValue_returnsSameHash() {
            CategoryName a = new CategoryName("CAMISAS");
            CategoryName b = new CategoryName("camisas");
            assertThat(a).hasSameHashCodeAs(b);
        }
    }
}