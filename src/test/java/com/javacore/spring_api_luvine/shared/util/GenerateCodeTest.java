package com.javacore.spring_api_luvine.shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GenerateCode")
class GenerateCodeTest {

    private final GenerateCode generateCode = new GenerateCode();

    // --- FORMATO DO CÓDIGO --------------------------------------------------

    @Nested
    @DisplayName("format()")
    class Format {

        @RepeatedTest(20)
        @DisplayName("deve sempre gerar exatamente 5 dígitos")
        void shouldAlwaysGenerateFiveDigits() {
            String code = generateCode.generate();
            assertThat(code).hasSize(6);
        }

        @RepeatedTest(20)
        @DisplayName("deve retornar apenas caracteres numéricos")
        void shouldContainOnlyDigits() {
            String code = generateCode.generate();
            assertThat(code).matches("\\d{6}");
        }

        @RepeatedTest(20)
        @DisplayName("resultado nunca deve ser null")
        void shouldNeverReturnNull() {
            assertThat(generateCode.generate()).isNotNull();
        }

        @RepeatedTest(20)
        @DisplayName("resultado nunca deve ser blank")
        void shouldNeverReturnBlank() {
            assertThat(generateCode.generate()).isNotBlank();
        }
    }

    // --- INTERVALO INVÁLIDO --------------------------------------------------

    @Nested
    @DisplayName("range()")
    class Range {

        @RepeatedTest(50)
        @DisplayName("deve gerar código maior ou igual a 100000 (limite inferior)")
        void shouldBeGreaterThanOrEqualToMinValue() {
            int code = Integer.parseInt(generateCode.generate());
            assertThat(code).isGreaterThanOrEqualTo(10_0000);
        }

        @RepeatedTest(50)
        @DisplayName("deve gerar código menor ou igual a 999999 (limite superior)")
        void shouldBeLessThanOrEqualToMaxValue() {
            int code = Integer.parseInt(generateCode.generate());
            assertThat(code).isLessThanOrEqualTo(99_9999);
        }

        @RepeatedTest(50)
        @DisplayName("deve gerar código dentro do intervalo [100000, 999999]")
        void shouldBeWithinValidRange() {
            int code = Integer.parseInt(generateCode.generate());
            assertThat(code).isBetween(10_0000, 99_9999);
        }

        @Test
        @DisplayName("nunca deve gerar código com menos de 6 dígitos (ex: 99999)")
        void shouldNeverGenerateCodeBelowMinimum() {
            for (int i = 0; i < 200; i++) {
                int code = Integer.parseInt(generateCode.generate());
                assertThat(code).isGreaterThanOrEqualTo(10_0000);
            }
        }
    }

    // --- ALEATORIEDADE --------------------------------------------------

    @Nested
    @DisplayName("randomness")
    class Randomness {

        @Test
        @DisplayName("deve gerar códigos distintos em chamadas consecutivas (distribuição)")
        void shouldGenerateDistinctCodesOverMultipleCalls() {
            Set<String> codes = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                codes.add(generateCode.generate());
            }
            assertThat(codes.size()).isGreaterThan(50);
        }

        @Test
        @DisplayName("não deve gerar sempre o mesmo código (não é constante)")
        void shouldNotAlwaysReturnSameCode() {
            String first = generateCode.generate();
            boolean foundDifferent = false;

            for (int i = 0; i < 20; i++) {
                if (!generateCode.generate().equals(first)) {
                    foundDifferent = true;
                    break;
                }
            }

            assertThat(foundDifferent)
                    .as("esperava ao menos um código diferente do primeiro em 20 tentativas")
                    .isTrue();
        }
    }
}