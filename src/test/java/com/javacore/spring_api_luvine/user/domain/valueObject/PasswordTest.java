package com.javacore.spring_api_luvine.user.domain.valueObject;

import com.javacore.spring_api_luvine.user.domain.exception.InvalidPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DisplayName("Password")
class PasswordTest {

    // --- CONSTRUTOR --------------------------------------------------------------

    @Nested
    @DisplayName("construtor")
    class Constructor {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("deve lançar InvalidPasswordException quando valor é nulo ou vazio")
        void constructor_nullOrEmpty_throwsInvalidPasswordException(String value) {
            assertThatExceptionOfType(InvalidPasswordException.class)
                    .isThrownBy(() -> new Password(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "   ",
                "\t",
                "\n"
        })
        @DisplayName("deve lançar InvalidPasswordException quando valor é apenas espaços em branco")
        void constructor_blankValue_throwsInvalidPasswordException(String value) {
            assertThatExceptionOfType(InvalidPasswordException.class)
                    .isThrownBy(() -> new Password(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "abc",
                "short1A",
                "Ab1@"
        })
        @DisplayName("deve lançar InvalidPasswordException quando senha tem menos de 8 caracteres")
        void constructor_tooShort_throwsInvalidPasswordException(String value) {
            assertThatExceptionOfType(InvalidPasswordException.class)
                    .isThrownBy(() -> new Password(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "password123@",
                "alllowercase1@",
                "nouppercase1!"
        })
        @DisplayName("deve lançar InvalidPasswordException quando senha não tem letra maiúscula")
        void constructor_noUpperCase_throwsInvalidPasswordException(String value) {
            assertThatExceptionOfType(InvalidPasswordException.class)
                    .isThrownBy(() -> new Password(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "PASSWORD123@",
                "ALLUPPERCASE1!",
                "NOLOWERCASE1#"
        })
        @DisplayName("deve lançar InvalidPasswordException quando senha não tem letra minúscula")
        void constructor_noLowerCase_throwsInvalidPasswordException(String value) {
            assertThatExceptionOfType(InvalidPasswordException.class)
                    .isThrownBy(() -> new Password(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "PasswordSemNum!",
                "NoNumberHere@Aa",
                "AbcDefGhI!#$%^"
        })
        @DisplayName("deve lançar InvalidPasswordException quando senha não tem número")
        void constructor_noDigit_throwsInvalidPasswordException(String value) {
            assertThatExceptionOfType(InvalidPasswordException.class)
                    .isThrownBy(() -> new Password(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Password123",
                "NoSpecialChar1A",
                "AbcDefGh1234"
        })
        @DisplayName("deve lançar InvalidPasswordException quando senha não tem caractere especial")
        void constructor_noSpecialChar_throwsInvalidPasswordException(String value) {
            assertThatExceptionOfType(InvalidPasswordException.class)
                    .isThrownBy(() -> new Password(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Password@123",
                "Str0ng!Pass",
                "V@lidPass1",
                "C0mpl3x#Pwd",
                "Abcdef1#gh"
        })
        @DisplayName("deve aceitar senha válida com maiúscula, minúscula, número e caractere especial")
        void constructor_validPassword_doesNotThrow(String value) {
            Password password = new Password(value);
            assertThat(password.value()).isEqualTo(value.trim());
        }

        @Test
        @DisplayName("deve remover espaços das bordas ao normalizar")
        void constructor_passwordWithSurroundingSpaces_trimsValue() {
            Password password = new Password("  Password@123  ");
            assertThat(password.value()).isEqualTo("Password@123");
        }
    }

    // --- EQUALS / HASHCODE -------------------------------------------------------

    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsAndHashCode {

        @Test
        @DisplayName("deve ser igual a outro Password com o mesmo valor")
        void equals_sameValue_returnsTrue() {
            Password a = new Password("Password@123");
            Password b = new Password("Password@123");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("deve ser diferente de outro Password com valor distinto")
        void equals_differentValue_returnsFalse() {
            Password a = new Password("Password@123");
            Password b = new Password("Differ3nt!Pwd");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("hashCode deve ser igual para senhas com o mesmo valor")
        void hashCode_sameValue_returnsSameHash() {
            Password a = new Password("Password@123");
            Password b = new Password("Password@123");
            assertThat(a).hasSameHashCodeAs(b);
        }
    }
}