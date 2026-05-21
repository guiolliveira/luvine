package com.javacore.spring_api_luvine.common.util;

import com.javacore.spring_api_luvine.common.exception.exceptions.TokenHashException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TokenHash")
class TokenHashTest {

    // --- HELPERS --------------------------------------------------

    private String expectedHash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(bytes);
    }

    // --- CORREÇÃO DO HASH --------------------------------------------------

    @Nested
    @DisplayName("correctness()")
    class Correctness {

        @Test
        @DisplayName("deve produzir o hash SHA-256 + Base64 correto para uma entrada conhecida")
        void shouldProduceCorrectHashForKnownInput() throws Exception {
            String token = "my-secure-token";
            assertThat(TokenHash.hash(token)).isEqualTo(expectedHash(token));
        }

        @Test
        @DisplayName("deve produzir hash determinístico — mesma entrada sempre gera mesmo hash")
        void shouldBeDeterministic() {
            String token = "deterministic-token-123";
            String first  = TokenHash.hash(token);
            String second = TokenHash.hash(token);
            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("deve produzir hashes diferentes para tokens diferentes")
        void shouldProduceDifferentHashesForDifferentInputs() {
            assertThat(TokenHash.hash("token-A")).isNotEqualTo(TokenHash.hash("token-B"));
        }

        @Test
        @DisplayName("deve ser sensível a maiúsculas/minúsculas")
        void shouldBeCaseSensitive() {
            assertThat(TokenHash.hash("Token")).isNotEqualTo(TokenHash.hash("token"));
        }

        @Test
        @DisplayName("deve ser sensível a espaços e caracteres extras")
        void shouldBeSensitiveToWhitespace() {
            assertThat(TokenHash.hash("token")).isNotEqualTo(TokenHash.hash("token "));
            assertThat(TokenHash.hash("token")).isNotEqualTo(TokenHash.hash(" token"));
        }

        @Test
        @DisplayName("deve hashear string vazia sem lançar exceção")
        void shouldHashEmptyStringWithoutException() throws Exception {
            assertThat(TokenHash.hash("")).isEqualTo(expectedHash(""));
        }

        @Test
        @DisplayName("deve hashear token muito longo corretamente")
        void shouldHashLongTokenCorrectly() throws Exception {
            String longToken = "a".repeat(10_000);
            assertThat(TokenHash.hash(longToken)).isEqualTo(expectedHash(longToken));
        }

        @Test
        @DisplayName("deve hashear caracteres especiais e Unicode corretamente")
        void shouldHashSpecialAndUnicodeCharsCorrectly() throws Exception {
            String specialToken = "tökén@!#$%^&*()_+áéíóú";
            assertThat(TokenHash.hash(specialToken)).isEqualTo(expectedHash(specialToken));
        }
    }

    // --- FORMATO DO RESULTADO --------------------------------------------------

    @Nested
    @DisplayName("outputFormat()")
    class OutputFormat {

        @Test
        @DisplayName("deve retornar string codificada em Base64 (apenas caracteres válidos)")
        void shouldReturnValidBase64String() {
            String result = TokenHash.hash("any-token");
            assertThat(result).matches("^[A-Za-z0-9+/=]+$");
        }

        @Test
        @DisplayName("deve retornar hash com 44 caracteres (SHA-256 = 32 bytes → Base64 = 44 chars)")
        void shouldReturnFortyFourChars() {
            assertThat(TokenHash.hash("any-token")).hasSize(44);
        }

        @Test
        @DisplayName("resultado nunca deve ser null")
        void shouldNeverReturnNull() {
            assertThat(TokenHash.hash("token")).isNotNull();
        }

        @Test
        @DisplayName("resultado nunca deve ser blank")
        void shouldNeverReturnBlank() {
            assertThat(TokenHash.hash("token")).isNotBlank();
        }

        @Test
        @DisplayName("hash resultante não deve conter o token original em texto puro")
        void shouldNotContainOriginalToken() {
            String token = "supersecrettoken";
            assertThat(TokenHash.hash(token)).doesNotContain(token);
        }
    }

    // --- FORMATO DE EXCEÇÃO --------------------------------------------------

    @Nested
    @DisplayName("exceptionHandling()")
    class ExceptionHandling {

        @Test
        @DisplayName("deve lançar TokenHashException quando token for null")
        void shouldThrowTokenHashExceptionForNullToken() {
            assertThatThrownBy(() -> TokenHash.hash(null))
                    .isInstanceOf(TokenHashException.class);
        }
    }
}