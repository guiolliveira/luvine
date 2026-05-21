package com.javacore.spring_api_luvine.user.application.usecase;

import com.javacore.spring_api_luvine.user.application.dto.AddressResponse;
import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.mapper.UserMapper;
import com.javacore.spring_api_luvine.user.domain.entity.Address;
import com.javacore.spring_api_luvine.user.infrastructure.repository.AddressRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@DisplayName("FindAllAddressesUseCase")
@ExtendWith(MockitoExtension.class)
class FindAllAddressesUseCaseTest {

    @Mock private AddressRepository addressRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private FindAllAddressesUseCase findAllAddressesUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PUBLIC_ID = UUID.randomUUID();

    private CurrentUser currentUser() {
        return new CurrentUser(PUBLIC_ID);
    }

    private AddressResponse buildAddressResponse() {
        return new AddressResponse(
                UUID.randomUUID(), "João", "Silva", "01310100",
                "Avenida Paulista", "1000", "Apto 42", "Bela Vista",
                "São Paulo", "SP", "Brasil", "11987654321", false
        );
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve retornar lista de endereços ativos do usuário")
        void execute_activeAddressesExist_returnsAddressResponseList() {
            Address address1 = mock(Address.class);
            Address address2 = mock(Address.class);
            AddressResponse response1 = buildAddressResponse();
            AddressResponse response2 = buildAddressResponse();

            given(addressRepository.findAllByUserPublicIdAndActiveTrue(PUBLIC_ID))
                    .willReturn(List.of(address1, address2));
            given(userMapper.toAddressResponse(address1)).willReturn(response1);
            given(userMapper.toAddressResponse(address2)).willReturn(response2);

            List<AddressResponse> result = findAllAddressesUseCase.execute(currentUser());

            assertThat(result).hasSize(2).containsExactly(response1, response2);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando usuário não possui endereços ativos")
        void execute_noActiveAddresses_returnsEmptyList() {
            given(addressRepository.findAllByUserPublicIdAndActiveTrue(PUBLIC_ID))
                    .willReturn(List.of());

            List<AddressResponse> result = findAllAddressesUseCase.execute(currentUser());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("deve mapear cada endereço usando o userMapper")
        void execute_activeAddressesExist_mapsEachAddressWithMapper() {
            Address address = mock(Address.class);

            given(addressRepository.findAllByUserPublicIdAndActiveTrue(PUBLIC_ID))
                    .willReturn(List.of(address));
            given(userMapper.toAddressResponse(address)).willReturn(buildAddressResponse());

            findAllAddressesUseCase.execute(currentUser());

            then(userMapper).should(times(1)).toAddressResponse(address);
        }

        @Test
        @DisplayName("deve buscar endereços usando o publicId correto da sessão")
        void execute_called_queriesRepositoryWithCorrectPublicId() {
            given(addressRepository.findAllByUserPublicIdAndActiveTrue(PUBLIC_ID))
                    .willReturn(List.of());

            findAllAddressesUseCase.execute(currentUser());

            then(addressRepository).should().findAllByUserPublicIdAndActiveTrue(PUBLIC_ID);
        }
    }
}