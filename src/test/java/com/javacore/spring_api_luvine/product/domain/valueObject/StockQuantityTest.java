package com.javacore.spring_api_luvine.product.domain.valueObject;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidStockQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DisplayName("StockQuantity")
class StockQuantityTest {

    // --- CONSTRUTOR --------------------------------------------------------------

    @Nested
    @DisplayName("construtor")
    class Constructor {

        @ParameterizedTest
        @ValueSource(ints = {-1, -10, -100, Integer.MIN_VALUE})
        @DisplayName("deve lançar InvalidStockQuantity quando valor é negativo")
        void constructor_negativeValue_throwsInvalidStockQuantity(int value) {
            assertThatExceptionOfType(InvalidStockQuantity.class)
                    .isThrownBy(() -> new StockQuantity(value));
        }

        @Test
        @DisplayName("deve aceitar zero como valor válido")
        void constructor_zero_doesNotThrow() {
            StockQuantity stock = new StockQuantity(0);
            assertThat(stock.value()).isZero();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 10, 100, Integer.MAX_VALUE})
        @DisplayName("deve aceitar qualquer valor positivo")
        void constructor_positiveValue_doesNotThrow(int value) {
            StockQuantity stock = new StockQuantity(value);
            assertThat(stock.value()).isEqualTo(value);
        }
    }

    // --- EQUALS / HASHCODE -------------------------------------------------------

    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsAndHashCode {

        @Test
        @DisplayName("deve ser igual a outro StockQuantity com o mesmo valor")
        void equals_sameValue_returnsTrue() {
            StockQuantity a = new StockQuantity(10);
            StockQuantity b = new StockQuantity(10);
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("deve ser diferente de outro StockQuantity com valor distinto")
        void equals_differentValue_returnsFalse() {
            StockQuantity a = new StockQuantity(10);
            StockQuantity b = new StockQuantity(20);
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("hashCode deve ser igual para StockQuantities com o mesmo valor")
        void hashCode_sameValue_returnsSameHash() {
            StockQuantity a = new StockQuantity(10);
            StockQuantity b = new StockQuantity(10);
            assertThat(a).hasSameHashCodeAs(b);
        }
    }
}