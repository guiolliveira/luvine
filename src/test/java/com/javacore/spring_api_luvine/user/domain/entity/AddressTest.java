package com.javacore.spring_api_luvine.user.domain.entity;

import com.javacore.spring_api_luvine.user.domain.valueObject.Cep;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
import com.javacore.spring_api_luvine.user.domain.valueObject.Phone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Address")
class AddressTest {

    // --- FIXTURE -------------------------------------------------------------

    private static final String FIRST_NAME = "João";
    private static final String LAST_NAME = "Silva";
    private static final String PHONE = "11987654321";
    private static final String CEP = "01310100";
    private static final String STREET = "Avenida Paulista";
    private static final String NUMBER = "1000";
    private static final String COMPLEMENT = "Apto 42";
    private static final String NEIGHBORHOOD = "Bela Vista";
    private static final String CITY = "São Paulo";
    private static final String STATE = "São Paulo";
    private static final String COUNTRY = "Brasil";

    private User anyUser() {
        return User.create(
                new Email("user@example.com"),
                new PersonName("João"),
                new PersonName("Silva"),
                "hashed-password",
                UserProvider.LOCAL
        );
    }

    private Address defaultAddress() {
        return Address.create(
                anyUser(),
                new PersonName(FIRST_NAME), new PersonName(LAST_NAME),
                new Cep(CEP), STREET, NUMBER, COMPLEMENT,
                NEIGHBORHOOD, CITY, STATE, COUNTRY,
                new Phone(PHONE), true
        );
    }

    private Address nonDefaultAddress() {
        return Address.create(
                anyUser(),
                new PersonName(FIRST_NAME), new PersonName(LAST_NAME),
                new Cep(CEP), STREET, NUMBER, COMPLEMENT,
                NEIGHBORHOOD, CITY, STATE, COUNTRY,
                new Phone(PHONE), false
        );
    }

    // --- ADDRESS.CREATE() - FACTORY ------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve persistir todos os campos escalares corretamente")
        void shouldPersistAllScalarFields() {
            Address address = defaultAddress();

            assertThat(address.getFirstName()).isEqualTo(new PersonName(FIRST_NAME));
            assertThat(address.getLastName()).isEqualTo(new PersonName(LAST_NAME));
            assertThat(address.getCep()).isEqualTo(new Cep(CEP));
            assertThat(address.getPhone()).isEqualTo(new Phone(PHONE));
            assertThat(address.getStreet()).isEqualTo(STREET);
            assertThat(address.getNumber()).isEqualTo(NUMBER);
            assertThat(address.getComplement()).isEqualTo(COMPLEMENT);
            assertThat(address.getNeighborhood()).isEqualTo(NEIGHBORHOOD);
            assertThat(address.getCity()).isEqualTo(CITY);
            assertThat(address.getState()).isEqualTo(STATE);
            assertThat(address.getCountry()).isEqualTo(COUNTRY);
        }

        @Test
        @DisplayName("deve associar o usuário recebido por parâmetro")
        void shouldAssociateUser() {
            User user = anyUser();
            Address address = Address.create(
                    user,
                    new PersonName(FIRST_NAME), new PersonName(LAST_NAME),
                    new Cep(CEP), STREET, NUMBER, COMPLEMENT,
                    NEIGHBORHOOD, CITY, STATE, COUNTRY,
                    new Phone(PHONE), false
            );

            assertThat(address.getUser()).isSameAs(user);
        }

        @Test
        @DisplayName("deve gerar publicId UUID não nulo")
        void shouldGenerateNonNullPublicId() {
            assertThat(defaultAddress().getPublicId()).isNotNull();
        }

        @Test
        @DisplayName("cada instância deve ter um publicId único")
        void shouldGenerateDistinctPublicIds() {
            Address a = defaultAddress();
            Address b = defaultAddress();

            assertThat(a.getPublicId()).isNotEqualTo(b.getPublicId());
        }

        @Test
        @DisplayName("deve iniciar com active = true")
        void shouldStartActive() {
            assertThat(defaultAddress().isActive()).isTrue();
        }

        @Test
        @DisplayName("deve iniciar com defaultAddress conforme parâmetro (true)")
        void shouldSetDefaultAddressWhenTrue() {
            assertThat(defaultAddress().isDefaultAddress()).isTrue();
        }

        @Test
        @DisplayName("deve iniciar com defaultAddress conforme parâmetro (false)")
        void shouldSetDefaultAddressWhenFalse() {
            assertThat(nonDefaultAddress().isDefaultAddress()).isFalse();
        }

        @Test
        @DisplayName("id deve ser nulo antes de persistência (sem banco)")
        void shouldHaveNullIdBeforePersistence() {
            assertThat(defaultAddress().getId()).isNull();
        }

        @Test
        @DisplayName("deve registrar createdAt próximo ao instante atual")
        void shouldSetCreatedAtNearNow() {
            Instant before = Instant.now();
            Address address = defaultAddress();
            Instant after = Instant.now();

            assertThat(address.getCreatedAt())
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("deve fazer trim de espaços nos campos de texto")
        void shouldTrimStringFields() {
            Address address = Address.create(
                    anyUser(),
                    new PersonName(FIRST_NAME), new PersonName(LAST_NAME),
                    new Cep(CEP),
                    "  " + STREET + "  ",
                    "  " + NUMBER + "  ",
                    "  " + COMPLEMENT + "  ",
                    "  " + NEIGHBORHOOD + "  ",
                    "  " + CITY + "  ",
                    "  " + STATE + "  ",
                    "  " + COUNTRY + "  ",
                    new Phone(PHONE), false
            );

            assertThat(address.getStreet()).isEqualTo(STREET);
            assertThat(address.getNumber()).isEqualTo(NUMBER);
            assertThat(address.getComplement()).isEqualTo(COMPLEMENT);
            assertThat(address.getNeighborhood()).isEqualTo(NEIGHBORHOOD);
            assertThat(address.getCity()).isEqualTo(CITY);
            assertThat(address.getState()).isEqualTo(STATE);
            assertThat(address.getCountry()).isEqualTo(COUNTRY);
        }

        @Test
        @DisplayName("deve aceitar complement nulo sem lançar exceção")
        void shouldAcceptNullComplement() {
            Address address = Address.create(
                    anyUser(),
                    new PersonName(FIRST_NAME), new PersonName(LAST_NAME),
                    new Cep(CEP), STREET, NUMBER, null,
                    NEIGHBORHOOD, CITY, STATE, COUNTRY,
                    new Phone(PHONE), false
            );

            assertThat(address.getComplement()).isNull();
        }
    }

    // --- MARKASDEFAULT() -----------------------------------------------------

    @Nested
    @DisplayName("markAsDefault()")
    class MarkAsDefault {

        @Test
        @DisplayName("deve alterar defaultAddress para true")
        void shouldSetDefaultAddressToTrue() {
            Address address = nonDefaultAddress();
            address.markAsDefault();

            assertThat(address.isDefaultAddress()).isTrue();
        }

        @Test
        @DisplayName("deve ser idempotente: chamar duas vezes não causa erro")
        void shouldBeIdempotent() {
            Address address = nonDefaultAddress();
            address.markAsDefault();
            address.markAsDefault();

            assertThat(address.isDefaultAddress()).isTrue();
        }

        @Test
        @DisplayName("não deve alterar active")
        void shouldNotAffectActive() {
            Address address = nonDefaultAddress();
            address.markAsDefault();

            assertThat(address.isActive()).isTrue();
        }

        @Test
        @DisplayName("não deve alterar outros campos")
        void shouldNotAffectOtherFields() {
            Address address = nonDefaultAddress();
            Cep cepBefore = address.getCep();

            address.markAsDefault();

            assertThat(address.getCep()).isEqualTo(cepBefore);
            assertThat(address.getCity()).isEqualTo(CITY);
            assertThat(address.getStreet()).isEqualTo(STREET);
        }
    }

    // --- DISABLE() -----------------------------------------------------------

    @Nested
    @DisplayName("disable()")
    class Disable {

        @Test
        @DisplayName("deve alterar active para false")
        void shouldSetActiveToFalse() {
            Address address = defaultAddress();
            address.disable();

            assertThat(address.isActive()).isFalse();
        }

        @Test
        @DisplayName("deve alterar defaultAddress para false")
        void shouldSetDefaultAddressToFalse() {
            Address address = defaultAddress();
            address.disable();

            assertThat(address.isDefaultAddress()).isFalse();
        }

        @Test
        @DisplayName("deve ser idempotente: chamar duas vezes não causa erro")
        void shouldBeIdempotent() {
            Address address = defaultAddress();
            address.disable();
            address.disable();

            assertThat(address.isActive()).isFalse();
            assertThat(address.isDefaultAddress()).isFalse();
        }

        @Test
        @DisplayName("não deve alterar outros campos")
        void shouldNotAffectOtherFields() {
            Address address = defaultAddress();
            UUID publicIdBefore = address.getPublicId();

            address.disable();

            assertThat(address.getPublicId()).isEqualTo(publicIdBefore);
            assertThat(address.getCity()).isEqualTo(CITY);
            assertThat(address.getStreet()).isEqualTo(STREET);
            assertThat(address.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("deve desabilitar endereço que já não era default")
        void shouldDisableNonDefaultAddress() {
            Address address = nonDefaultAddress();
            address.disable();

            assertThat(address.isActive()).isFalse();
            assertThat(address.isDefaultAddress()).isFalse();
        }
    }

    // --- FLUXO DE NEGÓCIO ----------------------------------------------------

    @Nested
    @DisplayName("Fluxo de gerenciamento de endereço")
    class AddressLifecycleFlow {

        @Test
        @DisplayName("fluxo completo: criar → promover a default → desabilitar")
        void fullLifecycleFlow() {
            Address address = nonDefaultAddress();

            assertThat(address.isDefaultAddress()).isFalse();
            assertThat(address.isActive()).isTrue();

            address.markAsDefault();
            assertThat(address.isDefaultAddress()).isTrue();
            assertThat(address.isActive()).isTrue();

            address.disable();
            assertThat(address.isDefaultAddress()).isFalse();
            assertThat(address.isActive()).isFalse();
        }

        @Test
        @DisplayName("dois endereços do mesmo usuário devem ser independentes")
        void twoAddressesShouldBeIndependent() {
            User user = anyUser();

            Address address1 = Address.create(user, new PersonName(FIRST_NAME), new PersonName(LAST_NAME),
                    new Cep(CEP), STREET, "100", null, NEIGHBORHOOD, CITY, STATE, COUNTRY,
                    new Phone(PHONE), true);

            Address address2 = Address.create(user, new PersonName(FIRST_NAME), new PersonName(LAST_NAME),
                    new Cep(CEP), STREET, "200", null, NEIGHBORHOOD, CITY, STATE, COUNTRY,
                    new Phone(PHONE), false);

            address1.disable();

            assertThat(address1.isActive()).isFalse();
            assertThat(address2.isActive()).isTrue();
            assertThat(address1.getPublicId()).isNotEqualTo(address2.getPublicId());
        }
    }
}