package com.javacore.spring_api_luvine.user.infrastucture.repository;

import com.javacore.spring_api_luvine.testcontainers.AbstractIntegrationTest;
import com.javacore.spring_api_luvine.user.domain.entity.Address;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Cep;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
import com.javacore.spring_api_luvine.user.domain.valueObject.Phone;
import com.javacore.spring_api_luvine.user.infrastructure.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("AddressRepository")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AddressRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    AddressRepository repository;

    User user;

    @BeforeEach
    void setUp() {
        user = em.persist(buildUser("user@example.com"));
        em.flush();
    }

    // --- HELPERS -------------------------------------------------------------

    private User buildUser(String email) {
        return User.create(new Email(email), new PersonName("João"), new PersonName("Silva"), "hashed", UserProvider.LOCAL);
    }

    private Address persistAddress(User owner, boolean defaultAddress) {
        Address address = Address.create(
                owner,
                new PersonName("João"), new PersonName("Silva"),
                new Cep("01310100"),
                "Avenida Paulista", "1000", "Apto 42",
                "Bela Vista", "São Paulo", "SP", "Brasil",
                new Phone("11987654321"),
                defaultAddress
        );
        em.persist(address);
        em.flush();
        return address;
    }

    private Address persistDisabledAddress(User owner) {
        Address address = persistAddress(owner, false);
        address.disable();
        em.flush();
        return address;
    }

    // --- RESETDEFAULTADDRESSFORUSER ------------------------------------------

    @Nested
    @DisplayName("resetDefaultAddressForUser()")
    class ResetDefaultAddressForUser {

        @Test
        @DisplayName("deve remover o flag default do endereço que era default")
        void shouldClearDefaultFlagFromCurrentDefaultAddress() {
            Address defaultAddr = persistAddress(user, true);

            repository.resetDefaultAddressForUser(user.getPublicId());
            em.clear();

            Address reloaded = repository.findById(defaultAddr.getId()).orElseThrow();
            assertThat(reloaded.isDefaultAddress()).isFalse();
        }

        @Test
        @DisplayName("deve remover o flag default de todos os endereços default do usuário")
        void shouldClearDefaultFlagFromAllDefaultAddresses() {
            Address first  = persistAddress(user, true);
            Address second = persistAddress(user, true);

            repository.resetDefaultAddressForUser(user.getPublicId());
            em.clear();

            Address reloadedFirst  = repository.findById(first.getId()).orElseThrow();
            Address reloadedSecond = repository.findById(second.getId()).orElseThrow();

            assertThat(reloadedFirst.isDefaultAddress()).isFalse();
            assertThat(reloadedSecond.isDefaultAddress()).isFalse();
        }

        @Test
        @DisplayName("não deve afetar endereços que já não eram default")
        void shouldNotAffectNonDefaultAddresses() {
            Address nonDefault = persistAddress(user, false);

            repository.resetDefaultAddressForUser(user.getPublicId());
            em.clear();

            Address reloaded = repository.findById(nonDefault.getId()).orElseThrow();
            assertThat(reloaded.isDefaultAddress()).isFalse();
        }

        @Test
        @DisplayName("deve afetar somente os endereços do usuário alvo")
        void shouldOnlyAffectTargetUser() {
            User otherUser = em.persist(buildUser("other@example.com"));
            em.flush();

            Address otherDefault = persistAddress(otherUser, true);
            persistAddress(user, true);

            repository.resetDefaultAddressForUser(user.getPublicId());
            em.clear();

            Address reloaded = repository.findById(otherDefault.getId()).orElseThrow();
            assertThat(reloaded.isDefaultAddress()).isTrue();
        }

        @Test
        @DisplayName("não deve afetar endereços inativos (disable = true)")
        void shouldNotAffectDisabledAddresses() {
            Address disabled = persistDisabledAddress(user);

            repository.resetDefaultAddressForUser(user.getPublicId());
            em.clear();

            Address reloaded = repository.findById(disabled.getId()).orElseThrow();
            assertThat(reloaded.isDefaultAddress()).isFalse();
            assertThat(reloaded.isActive()).isFalse();
        }

        @Test
        @DisplayName("não deve lançar exceção quando usuário não tiver endereços default")
        void shouldNotThrowWhenNoDefaultAddresses() {
            persistAddress(user, false);

            assertDoesNotThrow(() -> repository.resetDefaultAddressForUser(user.getPublicId()));
        }

        @Test
        @DisplayName("não deve lançar exceção quando usuário não tiver nenhum endereço")
        void shouldNotThrowWhenNoAddresses() {
            assertDoesNotThrow(() -> repository.resetDefaultAddressForUser(user.getPublicId()));
        }

        @Test
        @DisplayName("após reset, nenhum endereço do usuário deve ser default")
        void afterResetNoAddressShouldBeDefault() {
            persistAddress(user, true);
            persistAddress(user, true);
            persistAddress(user, false);

            repository.resetDefaultAddressForUser(user.getPublicId());
            em.clear();

            List<Address> addresses = repository.findAllByUserPublicIdAndActiveTrue(user.getPublicId());
            assertThat(addresses).isNotEmpty()
                    .allMatch(a -> !a.isDefaultAddress());
        }
    }
}