package com.javacore.spring_api_luvine.user.service;

import com.javacore.spring_api_luvine.user.domain.entity.Address;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.exception.AddressAlreadyExistsException;
import com.javacore.spring_api_luvine.user.domain.exception.AddressInactiveException;
import com.javacore.spring_api_luvine.user.domain.exception.AddressNotFoundException;
import com.javacore.spring_api_luvine.user.domain.exception.UserSessionInvalidException;
import com.javacore.spring_api_luvine.user.domain.valueObject.Cep;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
import com.javacore.spring_api_luvine.user.domain.valueObject.Phone;
import com.javacore.spring_api_luvine.user.application.dto.AddressRequest;
import com.javacore.spring_api_luvine.user.application.dto.AddressResponse;
import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.mapper.UserMapper;
import com.javacore.spring_api_luvine.user.infrastructure.repository.AddressRepository;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;


@DisplayName("AddressService")
@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private AddressService addressService;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID USER_PUBLIC_ID    = UUID.randomUUID();
    private static final UUID ADDRESS_PUBLIC_ID = UUID.randomUUID();

    private static final String FIRST_NAME   = "João";
    private static final String LAST_NAME    = "Silva";
    private static final String PHONE        = "11987654321";
    private static final String CEP          = "01310100";
    private static final String STREET       = "Avenida Paulista";
    private static final String NUMBER       = "1000";
    private static final String COMPLEMENT   = "Apto 42";
    private static final String NEIGHBORHOOD = "Bela Vista";
    private static final String CITY         = "São Paulo";
    private static final String STATE        = "São Paulo";
    private static final String COUNTRY      = "Brasil";

    private CurrentUser currentUser() {
        return new CurrentUser(USER_PUBLIC_ID);
    }

    private AddressRequest validRequest() {
        return new AddressRequest(
                FIRST_NAME, LAST_NAME, CEP, STREET, NUMBER, COMPLEMENT,
                NEIGHBORHOOD, CITY, STATE, COUNTRY, PHONE, false
        );
    }

    private AddressRequest validRequestAsDefault() {
        return new AddressRequest(
                FIRST_NAME, LAST_NAME, CEP, STREET, NUMBER, COMPLEMENT,
                NEIGHBORHOOD, CITY, STATE, COUNTRY, PHONE, true
        );
    }

    private User buildUser() {
        return User.create(
                new Email("user@example.com"),
                new PersonName("João"),
                new PersonName("Silva"),
                "hashed-password",
                UserProvider.LOCAL
        );
    }

    private Address buildAddress() {
        return mock(Address.class);
    }

    private Address buildDefaultAddress() {
        Address address = mock(Address.class);
        given(address.isDefaultAddress()).willReturn(true);
        return address;
    }

    // --- CREATE ADDRESS ------------------------------------------------------------------

    @Nested
    @DisplayName("createAddress()")
    class CreateAddress {

        @Test
        @DisplayName("deve criar endereço com sucesso e retornar AddressResponse")
        void createAddress_validRequest_returnsAddressResponse() {
            User user = buildUser();
            AddressResponse expectedResponse = mock(AddressResponse.class);

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(user));
            given(addressRepository.existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                    any(), any(), any(), any(), any(), any())).willReturn(false);
            given(addressRepository.existsByUserPublicIdAndActiveTrue(USER_PUBLIC_ID)).willReturn(true);
            given(userMapper.toAddressResponse(any())).willReturn(expectedResponse);

            AddressResponse response = addressService.createAddress(currentUser(), validRequest());

            assertThat(response).isNotNull().isEqualTo(expectedResponse);
            then(addressRepository).should().save(any(Address.class));
        }

        @Test
        @DisplayName("deve lançar UserSessionInvalidException quando usuário não encontrado")
        void createAddress_userNotFound_throwsUserSessionInvalidException() {
            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(UserSessionInvalidException.class)
                    .isThrownBy(() -> addressService.createAddress(currentUser(), validRequest()));

            then(addressRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar AddressAlreadyExistsException quando endereço já existe")
        void createAddress_duplicateAddress_throwsAddressAlreadyExistsException() {
            User user = buildUser();

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(user));
            given(addressRepository.existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                    any(), any(), any(), any(), any(), any())).willReturn(true);

            assertThatExceptionOfType(AddressAlreadyExistsException.class)
                    .isThrownBy(() -> addressService.createAddress(currentUser(), validRequest()));

            then(addressRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve definir endereço como default quando não há nenhum endereço ativo")
        void createAddress_noActiveAddress_setsAsDefault() {
            User user = buildUser();
            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(user));
            given(addressRepository.existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                    any(), any(), any(), any(), any(), any())).willReturn(false);
            given(addressRepository.existsByUserPublicIdAndActiveTrue(USER_PUBLIC_ID)).willReturn(false);
            given(userMapper.toAddressResponse(any())).willReturn(mock(AddressResponse.class));

            addressService.createAddress(currentUser(), validRequest());

            then(addressRepository).should().resetDefaultAddressForUser(USER_PUBLIC_ID);
            then(addressRepository).should().save(captor.capture());
            assertThat(captor.getValue().isDefaultAddress()).isTrue();
        }

        @Test
        @DisplayName("deve definir endereço como default quando request.defaultAddress = true")
        void createAddress_requestedAsDefault_setsAsDefault() {
            User user = buildUser();
            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(user));
            given(addressRepository.existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                    any(), any(), any(), any(), any(), any())).willReturn(false);
            given(addressRepository.existsByUserPublicIdAndActiveTrue(USER_PUBLIC_ID)).willReturn(true);
            given(userMapper.toAddressResponse(any())).willReturn(mock(AddressResponse.class));

            addressService.createAddress(currentUser(), validRequestAsDefault());

            then(addressRepository).should().resetDefaultAddressForUser(USER_PUBLIC_ID);
            then(addressRepository).should().save(captor.capture());
            assertThat(captor.getValue().isDefaultAddress()).isTrue();
        }

        @Test
        @DisplayName("não deve chamar resetDefaultAddressForUser quando endereço não é default")
        void createAddress_notDefault_doesNotResetDefault() {
            User user = buildUser();

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(user));
            given(addressRepository.existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                    any(), any(), any(), any(), any(), any())).willReturn(false);
            given(addressRepository.existsByUserPublicIdAndActiveTrue(USER_PUBLIC_ID)).willReturn(true);
            given(userMapper.toAddressResponse(any())).willReturn(mock(AddressResponse.class));

            addressService.createAddress(currentUser(), validRequest());

            then(addressRepository).should(never()).resetDefaultAddressForUser(any());
        }

        @Test
        @DisplayName("deve persistir endereço com os value objects corretos")
        void createAddress_validRequest_persistsCorrectValueObjects() {
            User user = buildUser();
            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(user));
            given(addressRepository.existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                    any(), any(), any(), any(), any(), any())).willReturn(false);
            given(addressRepository.existsByUserPublicIdAndActiveTrue(USER_PUBLIC_ID)).willReturn(true);
            given(userMapper.toAddressResponse(any())).willReturn(mock(AddressResponse.class));

            addressService.createAddress(currentUser(), validRequest());

            then(addressRepository).should().save(captor.capture());
            Address saved = captor.getValue();

            assertThat(saved.getFirstName()).isEqualTo(new PersonName(FIRST_NAME));
            assertThat(saved.getLastName()).isEqualTo(new PersonName(LAST_NAME));
            assertThat(saved.getPhone()).isEqualTo(new Phone(PHONE));
            assertThat(saved.getCep()).isEqualTo(new Cep(CEP));
            assertThat(saved.getCity()).isEqualTo(CITY);
            assertThat(saved.getState()).isEqualTo(STATE);
        }
    }

    // --- DELETE ADDRESS ------------------------------------------------------------------

    @Nested
    @DisplayName("deleteAddress()")
    class DeleteAddress {

        @Test
        @DisplayName("deve deletar endereço com sucesso")
        void deleteAddress_existingAddress_disablesAddress() {
            Address address = buildAddress();

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, USER_PUBLIC_ID))
                    .willReturn(Optional.of(address));

            assertThatNoException()
                    .isThrownBy(() -> addressService.deleteAddress(currentUser(), ADDRESS_PUBLIC_ID));

            then(address).should().disable();
        }

        @Test
        @DisplayName("deve lançar AddressNotFoundException quando endereço não existe ou não pertence ao usuário")
        void deleteAddress_addressNotFound_throwsAddressNotFoundException() {
            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, USER_PUBLIC_ID))
                    .willReturn(Optional.empty());

            assertThatExceptionOfType(AddressNotFoundException.class)
                    .isThrownBy(() -> addressService.deleteAddress(currentUser(), ADDRESS_PUBLIC_ID));
        }

        @Test
        @DisplayName("deve reatribuir default ao próximo endereço quando o deletado era default")
        void deleteAddress_defaultAddress_reassignsDefaultToNext() {
            Address defaultAddress = buildDefaultAddress();
            Address nextAddress = mock(Address.class);

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, USER_PUBLIC_ID))
                    .willReturn(Optional.of(defaultAddress));
            given(addressRepository.findFirstByUserPublicIdAndActiveTrueOrderByCreatedAtDesc(USER_PUBLIC_ID))
                    .willReturn(Optional.of(nextAddress));

            addressService.deleteAddress(currentUser(), ADDRESS_PUBLIC_ID);

            then(nextAddress).should().markAsDefault();
        }

        @Test
        @DisplayName("não deve reatribuir default quando o endereço deletado não era default")
        void deleteAddress_nonDefaultAddress_doesNotReassignDefault() {
            Address address = buildAddress();

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, USER_PUBLIC_ID))
                    .willReturn(Optional.of(address));

            addressService.deleteAddress(currentUser(), ADDRESS_PUBLIC_ID);

            then(addressRepository).should(never())
                    .findFirstByUserPublicIdAndActiveTrueOrderByCreatedAtDesc(any());
        }

        @Test
        @DisplayName("não deve lançar exceção quando não há próximo endereço para reatribuir default")
        void deleteAddress_defaultAddress_noNextAddress_doesNotThrow() {
            Address defaultAddress = buildDefaultAddress();

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, USER_PUBLIC_ID))
                    .willReturn(Optional.of(defaultAddress));
            given(addressRepository.findFirstByUserPublicIdAndActiveTrueOrderByCreatedAtDesc(USER_PUBLIC_ID))
                    .willReturn(Optional.empty());

            assertThatNoException()
                    .isThrownBy(() -> addressService.deleteAddress(currentUser(), ADDRESS_PUBLIC_ID));
        }
    }

    // --- SET DEFAULT ADDRESS ------------------------------------------------------------------

    @Nested
    @DisplayName("setDefaultAddress()")
    class SetDefaultAddress {

        @Test
        @DisplayName("deve definir novo endereço default e retornar AddressResponse")
        void setDefaultAddress_activeAddress_returnsAddressResponse() {
            Address address = buildAddress();
            given(address.isActive()).willReturn(true);
            AddressResponse expectedResponse = mock(AddressResponse.class);

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, USER_PUBLIC_ID))
                    .willReturn(Optional.of(address));
            given(userMapper.toAddressResponse(address)).willReturn(expectedResponse);

            AddressResponse response = addressService.setDefaultAddress(currentUser(), ADDRESS_PUBLIC_ID);

            assertThat(response).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("deve lançar AddressNotFoundException quando endereço não existe")
        void setDefaultAddress_addressNotFound_throwsAddressNotFoundException() {
            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, USER_PUBLIC_ID))
                    .willReturn(Optional.empty());

            assertThatExceptionOfType(AddressNotFoundException.class)
                    .isThrownBy(() -> addressService.setDefaultAddress(currentUser(), ADDRESS_PUBLIC_ID));

            then(addressRepository).should(never()).resetDefaultAddressForUser(any());
        }

        @Test
        @DisplayName("deve lançar AddressInactiveException quando endereço está inativo")
        void setDefaultAddress_inactiveAddress_throwsAddressInactiveException() {
            Address inactiveAddress = mock(Address.class);
            given(inactiveAddress.isActive()).willReturn(false);

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, USER_PUBLIC_ID))
                    .willReturn(Optional.of(inactiveAddress));

            assertThatExceptionOfType(AddressInactiveException.class)
                    .isThrownBy(() -> addressService.setDefaultAddress(currentUser(), ADDRESS_PUBLIC_ID));

            then(addressRepository).should(never()).resetDefaultAddressForUser(any());
            then(inactiveAddress).should(never()).markAsDefault();
        }

        @Test
        @DisplayName("deve chamar resetDefaultAddressForUser antes de marcar o novo default")
        void setDefaultAddress_activeAddress_resetsBeforeMarking() {
            Address address = buildAddress();
            given(address.isActive()).willReturn(true);

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, USER_PUBLIC_ID))
                    .willReturn(Optional.of(address));
            given(userMapper.toAddressResponse(address)).willReturn(mock(AddressResponse.class));

            addressService.setDefaultAddress(currentUser(), ADDRESS_PUBLIC_ID);

            org.mockito.InOrder order = org.mockito.Mockito.inOrder(addressRepository, address);
            order.verify(addressRepository).resetDefaultAddressForUser(USER_PUBLIC_ID);
            order.verify(address).markAsDefault();
        }
    }

    // --- FIND ALL ADDRESSES ------------------------------------------------------------------

    @Nested
    @DisplayName("findAllAddresses()")
    class FindAllAddresses {

        @Test
        @DisplayName("deve retornar lista de endereços ativos do usuário")
        void findAllAddresses_existingAddresses_returnsMappedList() {
            Address a1 = mock(Address.class);
            Address a2 = mock(Address.class);
            AddressResponse r1 = mock(AddressResponse.class);
            AddressResponse r2 = mock(AddressResponse.class);

            given(addressRepository.findAllByUserPublicIdAndActiveTrue(USER_PUBLIC_ID))
                    .willReturn(List.of(a1, a2));
            given(userMapper.toAddressResponse(a1)).willReturn(r1);
            given(userMapper.toAddressResponse(a2)).willReturn(r2);

            List<AddressResponse> responses = addressService.findAllAddresses(currentUser());

            assertThat(responses).hasSize(2).containsExactly(r1, r2);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando usuário não tem endereços ativos")
        void findAllAddresses_noActiveAddresses_returnsEmptyList() {
            given(addressRepository.findAllByUserPublicIdAndActiveTrue(USER_PUBLIC_ID))
                    .willReturn(List.of());

            List<AddressResponse> responses = addressService.findAllAddresses(currentUser());

            assertThat(responses).isEmpty();
            then(userMapper).should(never()).toAddressResponse(any());
        }

        @Test
        @DisplayName("deve mapear cada endereço exatamente uma vez")
        void findAllAddresses_multipleAddresses_mapsEachOnce() {
            Address a1 = mock(Address.class);
            Address a2 = mock(Address.class);
            Address a3 = mock(Address.class);

            given(addressRepository.findAllByUserPublicIdAndActiveTrue(USER_PUBLIC_ID))
                    .willReturn(List.of(a1, a2, a3));
            given(userMapper.toAddressResponse(any())).willReturn(mock(AddressResponse.class));

            addressService.findAllAddresses(currentUser());

            then(userMapper).should(times(3)).toAddressResponse(any());
        }
    }
}