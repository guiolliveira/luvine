package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidMoneyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DisplayName("Money")
class MoneyTest {

    // --- CRIAÇÃO VÁLIDA -------------------------------------------------------------

    @Nested
    @DisplayName("valores válidos")
    class ValidValues {

        @Test
        @DisplayName("deve criar com valor positivo")
        void shouldCreateWithPositiveValue() {
            assertThat(new Money(new BigDecimal("99.90")).value())
                    .isEqualByComparingTo(new BigDecimal("99.90"));
        }

        @Test
        @DisplayName("deve aceitar valor zero")
        void shouldAcceptZero() {
            assertThat(new Money(BigDecimal.ZERO).value())
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("deve normalizar para 2 casas decimais com arredondamento HALF_UP")
        void shouldNormalizeToTwoDecimalPlaces() {
            assertThat(new Money(new BigDecimal("99.9")).value())
                    .isEqualByComparingTo(new BigDecimal("99.90"));
        }

        @Test
        @DisplayName("deve arredondar para cima corretamente")
        void shouldRoundHalfUp() {
            assertThat(new Money(new BigDecimal("99.995")).value())
                    .isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("dois Moneys com mesmo valor devem ser iguais")
        void shouldBeEqualWhenSameValue() {
            assertThat(new Money(new BigDecimal("99.90")))
                    .isEqualTo(new Money(new BigDecimal("99.9")));
        }

        @Test
        @DisplayName("dois Moneys com valores diferentes não devem ser iguais")
        void shouldNotBeEqualWhenDifferentValues() {
            assertThat(new Money(new BigDecimal("99.90")))
                    .isNotEqualTo(new Money(new BigDecimal("49.90")));
        }
    }

    // --- CRIAÇÃO INVÁLIDA -------------------------------------------------------------

    @Nested
    @DisplayName("valores inválidos")
    class InvalidValues {

        @Test
        @DisplayName("deve lançar InvalidMoneyException quando null")
        void shouldThrowWhenNull() {
            assertThatExceptionOfType(InvalidMoneyException.class)
                    .isThrownBy(() -> new Money(null));
        }

        @Test
        @DisplayName("deve lançar InvalidMoneyException para valor negativo")
        void shouldThrowWhenNegative() {
            assertThatExceptionOfType(InvalidMoneyException.class)
                    .isThrownBy(() -> new Money(new BigDecimal("-0.01")));
        }

        @Test
        @DisplayName("deve lançar InvalidMoneyException para valor muito negativo")
        void shouldThrowWhenVeryNegative() {
            assertThatExceptionOfType(InvalidMoneyException.class)
                    .isThrownBy(() -> new Money(new BigDecimal("-100.00")));
        }
    }
}