package com.javacore.spring_api_luvine.user.application.usecase;

import com.javacore.spring_api_luvine.user.application.dto.AddressResponse;
import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.mapper.UserMapper;
import com.javacore.spring_api_luvine.user.domain.entity.Address;
import com.javacore.spring_api_luvine.user.domain.exception.AddressInactiveException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("SetDefaultAddressUseCase")
@ExtendWith(MockitoExtension.class)
class SetDefaultAddressUseCaseTest {

    @Mock private AddressRepository addressRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private SetDefaultAddressUseCase setDefaultAddressUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PUBLIC_ID = UUID.randomUUID();
    private static final UUID ADDRESS_PUBLIC_ID = UUID.randomUUID();

    private CurrentUser currentUser() {
        return new CurrentUser(PUBLIC_ID);
    }

    private Address mockActiveAddress() {
        Address address = mock(Address.class);
        given(address.isActive()).willReturn(true);
        return address;
    }

    private AddressResponse buildAddressResponse() {
        return new AddressResponse(
                ADDRESS_PUBLIC_ID, "João", "Silva", "01310100",
                "Avenida Paulista", "1000", "Apto 42", "Bela Vista",
                "São Paulo", "SP", "Brasil", "11987654321", true
        );
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve definir endereço como padrão e retornar AddressResponse")
        void execute_activeAddress_returnsAddressResponse() {
            Address address = mockActiveAddress();
            AddressResponse expected = buildAddressResponse();

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, PUBLIC_ID))
                    .willReturn(Optional.of(address));
            given(userMapper.toAddressResponse(address)).willReturn(expected);

            AddressResponse result = setDefaultAddressUseCase.execute(currentUser(), ADDRESS_PUBLIC_ID);

            assertThat(result).isNotNull().isEqualTo(expected);
        }

        @Test
        @DisplayName("deve lançar AddressNotFoundException quando endereço não encontrado")
        void execute_addressNotFound_throwsAddressNotFoundException() {
            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, PUBLIC_ID))
                    .willReturn(Optional.empty());

            assertThatExceptionOfType(AddressNotFoundException.class)
                    .isThrownBy(() -> setDefaultAddressUseCase.execute(currentUser(), ADDRESS_PUBLIC_ID));

            then(addressRepository).should(never()).resetDefaultAddressForUser(any());
        }

        @Test
        @DisplayName("deve lançar AddressInactiveException quando endereço está inativo")
        void execute_inactiveAddress_throwsAddressInactiveException() {
            Address address = mock(Address.class);
            given(address.isActive()).willReturn(false);

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, PUBLIC_ID))
                    .willReturn(Optional.of(address));

            assertThatExceptionOfType(AddressInactiveException.class)
                    .isThrownBy(() -> setDefaultAddressUseCase.execute(currentUser(), ADDRESS_PUBLIC_ID));

            then(addressRepository).should(never()).resetDefaultAddressForUser(any());
            then(address).should(never()).markAsDefault();
        }

        @Test
        @DisplayName("deve resetar o padrão anterior antes de marcar o novo")
        void execute_activeAddress_resetsDefaultBeforeMarkingNew() {
            Address address = mockActiveAddress();

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, PUBLIC_ID))
                    .willReturn(Optional.of(address));
            given(userMapper.toAddressResponse(address)).willReturn(buildAddressResponse());

            setDefaultAddressUseCase.execute(currentUser(), ADDRESS_PUBLIC_ID);

            then(addressRepository).should().resetDefaultAddressForUser(PUBLIC_ID);
            then(address).should().markAsDefault();
        }

        @Test
        @DisplayName("deve chamar markAsDefault no endereço encontrado")
        void execute_activeAddress_marksAddressAsDefault() {
            Address address = mockActiveAddress();

            given(addressRepository.findByPublicIdAndUserPublicId(ADDRESS_PUBLIC_ID, PUBLIC_ID))
                    .willReturn(Optional.of(address));
            given(userMapper.toAddressResponse(address)).willReturn(buildAddressResponse());

            setDefaultAddressUseCase.execute(currentUser(), ADDRESS_PUBLIC_ID);

            then(address).should().markAsDefault();
        }
    }
}