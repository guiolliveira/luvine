package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidSlugException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DisplayName("Slug")
class SlugTest {

    // --- CONSTRUTOR --------------------------------------------------------------

    @Nested
    @DisplayName("construtor")
    class Constructor {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("deve lançar InvalidSlugException quando valor é nulo ou vazio")
        void constructor_nullOrEmpty_throwsInvalidSlugException(String value) {
            assertThatExceptionOfType(InvalidSlugException.class)
                    .isThrownBy(() -> new Slug(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("deve lançar InvalidSlugException quando valor é apenas espaços em branco")
        void constructor_blankValue_throwsInvalidSlugException(String value) {
            assertThatExceptionOfType(InvalidSlugException.class)
                    .isThrownBy(() -> new Slug(value));
        }

        @Test
        @DisplayName("deve lançar InvalidSlugException quando slug normalizado excede 150 caracteres")
        void constructor_tooLong_throwsInvalidSlugException() {
            String tooLong = "a".repeat(151);
            assertThatExceptionOfType(InvalidSlugException.class)
                    .isThrownBy(() -> new Slug(tooLong));
        }

        @Test
        @DisplayName("deve lançar InvalidSlugException quando valor resulta em string vazia após normalização")
        void constructor_onlySpecialChars_throwsInvalidSlugException() {
            assertThatExceptionOfType(InvalidSlugException.class)
                    .isThrownBy(() -> new Slug("@#$%!"));
        }

        @Test
        @DisplayName("deve aceitar slug simples já normalizado")
        void constructor_alreadyNormalizedSlug_doesNotThrow() {
            Slug slug = new Slug("camisas-polo");
            assertThat(slug.value()).isEqualTo("camisas-polo");
        }

        @Test
        @DisplayName("deve aceitar slug com números")
        void constructor_slugWithNumbers_doesNotThrow() {
            Slug slug = new Slug("produto-123");
            assertThat(slug.value()).isEqualTo("produto-123");
        }
    }

    // --- NORMALIZAÇÃO ------------------------------------------------------------

    @Nested
    @DisplayName("normalization()")
    class Normalization {

        @Test
        @DisplayName("deve converter para minúsculas")
        void constructor_uppercase_convertsToLowercase() {
            Slug slug = new Slug("CAMISAS-POLO");
            assertThat(slug.value()).isEqualTo("camisas-polo");
        }

        @Test
        @DisplayName("deve converter espaços em hífens")
        void constructor_withSpaces_convertsSpacesToHyphens() {
            Slug slug = new Slug("camisas polo masculino");
            assertThat(slug.value()).isEqualTo("camisas-polo-masculino");
        }

        @Test
        @DisplayName("deve remover acentos e caracteres especiais")
        void constructor_withAccents_removesAccents() {
            Slug slug = new Slug("camisão de verão");
            assertThat(slug.value()).isEqualTo("camisao-de-verao");
        }

        @Test
        @DisplayName("deve remover hífens duplicados")
        void constructor_multipleHyphens_collapsesToSingle() {
            Slug slug = new Slug("camisas--polo");
            assertThat(slug.value()).isEqualTo("camisas-polo");
        }

        @Test
        @DisplayName("deve remover hífens nas bordas")
        void constructor_leadingTrailingHyphens_removesEdgeHyphens() {
            Slug slug = new Slug("-camisas-polo-");
            assertThat(slug.value()).isEqualTo("camisas-polo");
        }

        @Test
        @DisplayName("deve remover espaços nas bordas antes de normalizar")
        void constructor_surroundingSpaces_trimsBeforeNormalizing() {
            Slug slug = new Slug("  camisas polo  ");
            assertThat(slug.value()).isEqualTo("camisas-polo");
        }

        @Test
        @DisplayName("deve normalizar combinação de maiúsculas, acentos e espaços")
        void constructor_mixedInput_normalizesCompletely() {
            Slug slug = new Slug("  Camisão   POLO  ");
            assertThat(slug.value()).isEqualTo("camisao-polo");
        }

        @Test
        @DisplayName("valor armazenado nunca deve conter letras maiúsculas")
        void constructor_anyInput_neverHasUppercase() {
            Slug slug = new Slug("CamisasPolo");
            assertThat(slug.value()).isEqualTo(slug.value().toLowerCase());
        }

        @Test
        @DisplayName("valor armazenado nunca deve começar ou terminar com hífen")
        void constructor_anyInput_neverHasEdgeHyphens() {
            Slug slug = new Slug("camisas polo");
            assertThat(slug.value())
                    .doesNotStartWith("-")
                    .doesNotEndWith("-");
        }
    }

    // --- EQUALS / HASHCODE -------------------------------------------------------

    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsAndHashCode {

        @Test
        @DisplayName("deve ser igual a outro Slug com o mesmo valor")
        void equals_sameValue_returnsTrue() {
            Slug a = new Slug("camisas-polo");
            Slug b = new Slug("camisas-polo");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("dois Slugs normalizados para o mesmo valor devem ser iguais")
        void equals_sameValueAfterNormalization_returnsTrue() {
            Slug a = new Slug("CAMISAS POLO");
            Slug b = new Slug("camisas-polo");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("deve ser diferente de outro Slug com valor distinto")
        void equals_differentValue_returnsFalse() {
            Slug a = new Slug("camisas-polo");
            Slug b = new Slug("calcas-jeans");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("hashCode deve ser igual para Slugs com mesmo valor normalizado")
        void hashCode_sameNormalizedValue_returnsSameHash() {
            Slug a = new Slug("CAMISAS POLO");
            Slug b = new Slug("camisas-polo");
            assertThat(a).hasSameHashCodeAs(b);
        }
    }
}