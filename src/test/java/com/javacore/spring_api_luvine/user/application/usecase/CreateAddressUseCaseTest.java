package com.javacore.spring_api_luvine.user.application.usecase;

import com.javacore.spring_api_luvine.user.application.dto.AddressRequest;
import com.javacore.spring_api_luvine.user.application.dto.AddressResponse;
import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.mapper.UserMapper;
import com.javacore.spring_api_luvine.user.domain.entity.Address;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.exception.AddressAlreadyExistsException;
import com.javacore.spring_api_luvine.user.domain.exception.UserSessionInvalidException;
import com.javacore.spring_api_luvine.user.domain.valueObject.Cep;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@DisplayName("CreateAddressUseCase")
@ExtendWith(MockitoExtension.class)
class CreateAddressUseCaseTest {

    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private CreateAddressUseCase createAddressUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PUBLIC_ID = UUID.randomUUID();

    private static final String FIRST_NAME = "João";
    private static final String LAST_NAME = "Silva";
    private static final String CEP = "01310100";
    private static final String STREET = "Avenida Paulista";
    private static final String NUMBER = "1000";
    private static final String COMPLEMENT = "Apto 42";
    private static final String NEIGHBORHOOD = "Bela Vista";
    private static final String CITY = "São Paulo";
    private static final String STATE = "SP";
    private static final String COUNTRY = "Brasil";
    private static final String PHONE = "11987654321";

    private CurrentUser currentUser() {
        return new CurrentUser(PUBLIC_ID);
    }

    private AddressRequest validRequest() {
        return validRequest(false);
    }

    private AddressRequest validRequest(boolean defaultAddress) {
        return new AddressRequest(
                FIRST_NAME, LAST_NAME, CEP,
                STREET, NUMBER, COMPLEMENT,
                NEIGHBORHOOD, CITY, STATE,
                COUNTRY, PHONE, defaultAddress
        );
    }

    private User mockUser() {
        return mock(User.class);
    }

    private AddressResponse buildAddressResponse() {
        return new AddressResponse(
                UUID.randomUUID(), FIRST_NAME, LAST_NAME, CEP,
                STREET, NUMBER, COMPLEMENT, NEIGHBORHOOD,
                CITY, STATE, COUNTRY, PHONE, false
        );
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve criar endereço com sucesso e retornar AddressResponse")
        void execute_validRequest_returnsAddressResponse() {
            AddressResponse expected = buildAddressResponse();

            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(mockUser()));
            given(addressRepository.existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                    any(), any(), any(), any(), any(), any())).willReturn(false);
            given(addressRepository.existsByUserPublicIdAndActiveTrue(PUBLIC_ID)).willReturn(false);
            given(userMapper.toAddressResponse(any(Address.class))).willReturn(expected);

            AddressResponse response = createAddressUseCase.execute(currentUser(), validRequest());

            assertThat(response).isNotNull().isEqualTo(expected);
            then(addressRepository).should().save(any(Address.class));
        }

        @Test
        @DisplayName("deve lançar UserSessionInvalidException quando usuário não encontrado")
        void execute_userNotFound_throwsUserSessionInvalidException() {
            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(UserSessionInvalidException.class)
                    .isThrownBy(() -> createAddressUseCase.execute(currentUser(), validRequest()));

            then(addressRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar AddressAlreadyExistsException quando endereço já cadastrado")
        void execute_duplicateAddress_throwsAddressAlreadyExistsException() {
            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(mockUser()));
            given(addressRepository.existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                    any(), any(), any(), any(), any(), any())).willReturn(true);

            assertThatExceptionOfType(AddressAlreadyExistsException.class)
                    .isThrownBy(() -> createAddressUseCase.execute(currentUser(), validRequest()));

            then(addressRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve definir endereço como padrão quando é o primeiro endereço do usuário")
        void execute_firstAddress_setsAsDefault() {
            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(mockUser()));
            given(addressRepository.existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                    any(), any(), any(), any(), any(), any())).willReturn(false);
            given(addressRepository.existsByUserPublicIdAndActiveTrue(PUBLIC_ID)).willReturn(false);

            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
            given(userMapper.toAddressResponse(captor.capture())).willReturn(buildAddressResponse());

            createAddressUseCase.execute(currentUser(), validRequest(false));

            assertThat(captor.getValue().isDefaultAddress()).isTrue();
        }

        @Test
        @DisplayName("deve resetar o endereço padrão anterior quando é o primeiro endereço")
        void execute_firstAddress_resetsDefaultAddressForUser() {
            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(mockUser()));
            given(addressRepository.existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                    any(), any(), any(), any(), any(), any())).willReturn(false);
            given(addressRepository.existsByUserPublicIdAndActiveTrue(PUBLIC_ID)).willReturn(false);
            given(userMapper.toAddressResponse(any())).willReturn(buildAddressResponse());

            createAddressUseCase.execute(currentUser(), validRequest());

            then(addressRepository).should().resetDefaultAddressForUser(PUBLIC_ID);
        }

        @Test
        @DisplayName("deve definir como padrão e resetar anteriores quando request.defaultAddress é true")
        void execute_requestedAsDefault_setsAsDefaultEvenWhenOthersExist() {
            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(mockUser()));
            given(addressRepository.existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                    any(), any(), any(), any(), any(), any())).willReturn(false);
            given(addressRepository.existsByUserPublicIdAndActiveTrue(PUBLIC_ID)).willReturn(true);

            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
            given(userMapper.toAddressResponse(captor.capture())).willReturn(buildAddressResponse());

            createAddressUseCase.execute(currentUser(), validRequest(true));

            assertThat(captor.getValue().isDefaultAddress()).isTrue();
            then(addressRepository).should().resetDefaultAddressForUser(PUBLIC_ID);
        }

        @Test
        @DisplayName("não deve resetar padrão nem marcar como padrão quando não é o primeiro e defaultAddress é false")
        void execute_notFirstAndNotRequested_doesNotResetDefault() {
            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(mockUser()));
            given(addressRepository.existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                    any(), any(), any(), any(), any(), any())).willReturn(false);
            given(addressRepository.existsByUserPublicIdAndActiveTrue(PUBLIC_ID)).willReturn(true);

            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
            given(userMapper.toAddressResponse(captor.capture())).willReturn(buildAddressResponse());

            createAddressUseCase.execute(currentUser(), validRequest(false));

            assertThat(captor.getValue().isDefaultAddress()).isFalse();
            then(addressRepository).should(never()).resetDefaultAddressForUser(any());
        }

        @Test
        @DisplayName("deve persistir o endereço exatamente uma vez")
        void execute_validRequest_savesAddressExactlyOnce() {
            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(mockUser()));
            given(addressRepository.existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                    any(), any(), any(), any(), any(), any())).willReturn(false);
            given(addressRepository.existsByUserPublicIdAndActiveTrue(PUBLIC_ID)).willReturn(false);
            given(userMapper.toAddressResponse(any())).willReturn(buildAddressResponse());

            createAddressUseCase.execute(currentUser(), validRequest());

            then(addressRepository).should(times(1)).save(any(Address.class));
        }

        @Test
        @DisplayName("deve lançar exceção quando CEP é inválido")
        void execute_invalidCep_throwsException() {
            AddressRequest request = new AddressRequest(
                    FIRST_NAME, LAST_NAME, "cep-invalido",
                    STREET, NUMBER, COMPLEMENT,
                    NEIGHBORHOOD, CITY, STATE,
                    COUNTRY, PHONE, false
            );

            assertThatException().isThrownBy(() -> createAddressUseCase.execute(currentUser(), request));
            then(addressRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando telefone é inválido")
        void execute_invalidPhone_throwsException() {
            AddressRequest request = new AddressRequest(
                    FIRST_NAME, LAST_NAME, CEP,
                    STREET, NUMBER, COMPLEMENT,
                    NEIGHBORHOOD, CITY, STATE,
                    COUNTRY, "telefone-invalido", false
            );

            assertThatException().isThrownBy(() -> createAddressUseCase.execute(currentUser(), request));
            then(addressRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve verificar duplicidade usando o publicId correto da sessão")
        void execute_validRequest_checksExistenceWithCorrectPublicId() {
            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(mockUser()));
            given(addressRepository.existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                    eq(PUBLIC_ID),
                    eq(new PersonName(FIRST_NAME)),
                    eq(new PersonName(LAST_NAME)),
                    eq(new Cep(CEP)),
                    eq(NUMBER),
                    eq(COMPLEMENT)
            )).willReturn(false);
            given(addressRepository.existsByUserPublicIdAndActiveTrue(PUBLIC_ID)).willReturn(false);
            given(userMapper.toAddressResponse(any())).willReturn(buildAddressResponse());

            createAddressUseCase.execute(currentUser(), validRequest());

            then(addressRepository).should()
                    .existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                            eq(PUBLIC_ID),
                            eq(new PersonName(FIRST_NAME)),
                            eq(new PersonName(LAST_NAME)),
                            eq(new Cep(CEP)),
                            eq(NUMBER),
                            eq(COMPLEMENT)
                    );
        }
    }
}