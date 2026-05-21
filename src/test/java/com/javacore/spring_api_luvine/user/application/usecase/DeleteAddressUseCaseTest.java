package com.javacore.spring_api_luvine.user.application.usecase;

import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.domain.entity.Address;
import com.javacore.spring_api_luvine.user.domain.exception.AddressNotFoundException;
import com.javacore.spring_api_luvine.user.infrastructure.repository.AddressRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("DeleteAddressUseCase")
@ExtendWith(MockitoExtension.class)
class DeleteAddressUseCaseTest {

    @Mock private AddressRepository addressRepository;

    @InjectMocks
    private DeleteAddressUseCase deleteAddressUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PUBLIC_ID = UUID.randomUUID();
    private static final UUID ADDRESS_PUBLIC_ID = UUID.randomUUID();

    private CurrentUser currentUser() {
        return new CurrentUser(PUBLIC_ID);
    }

    private Address mockAddress(boolean defaultAddress) {
        Address address = mock(Address.class);
        given(address.isDefaultAddress()).willReturn(defaultAddress);
        return address;
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve lançar AddressNotFoundException quando endereço não encontrado")
        void execute_addressNotFound_throwsAddressNotFoundException() {
            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, PUBLIC_ID))
                    .willReturn(Optional.empty());

            assertThatExceptionOfType(AddressNotFoundException.class)
                    .isThrownBy(() -> deleteAddressUseCase.execute(currentUser(), ADDRESS_PUBLIC_ID));
        }

        @Test
        @DisplayName("deve desativar o endereço quando encontrado")
        void execute_addressFound_disablesAddress() {
            Address address = mockAddress(false);

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, PUBLIC_ID))
                    .willReturn(Optional.of(address));

            deleteAddressUseCase.execute(currentUser(), ADDRESS_PUBLIC_ID);

            then(address).should().disable();
        }

        @Test
        @DisplayName("não deve reatribuir padrão quando endereço deletado não era padrão")
        void execute_nonDefaultAddress_doesNotReassignDefault() {
            Address address = mockAddress(false);

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, PUBLIC_ID))
                    .willReturn(Optional.of(address));

            deleteAddressUseCase.execute(currentUser(), ADDRESS_PUBLIC_ID);

            then(addressRepository).should(never())
                    .findFirstByUserPublicIdAndActiveTrueOrderByCreatedAtDesc(any());
        }

        @Test
        @DisplayName("deve reatribuir padrão ao próximo endereço quando o deletado era padrão")
        void execute_defaultAddress_withNextAvailable_reassignsDefault() {
            Address address = mockAddress(true);
            Address next = mock(Address.class);

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, PUBLIC_ID))
                    .willReturn(Optional.of(address));
            given(addressRepository.findFirstByUserPublicIdAndActiveTrueOrderByCreatedAtDesc(PUBLIC_ID))
                    .willReturn(Optional.of(next));

            deleteAddressUseCase.execute(currentUser(), ADDRESS_PUBLIC_ID);

            then(next).should().markAsDefault();
        }

        @Test
        @DisplayName("não deve lançar exceção quando endereço padrão deletado sem próximo disponível")
        void execute_defaultAddress_withoutNext_doesNotThrow() {
            Address address = mockAddress(true);

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, PUBLIC_ID))
                    .willReturn(Optional.of(address));
            given(addressRepository.findFirstByUserPublicIdAndActiveTrueOrderByCreatedAtDesc(PUBLIC_ID))
                    .willReturn(Optional.empty());

            deleteAddressUseCase.execute(currentUser(), ADDRESS_PUBLIC_ID);

            then(address).should().disable();
        }
    }
}