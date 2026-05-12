package com.javacore.spring_api_luvine.user.service;

import com.javacore.spring_api_luvine.shared.dto.MessageResponse;
import com.javacore.spring_api_luvine.user.domain.entity.Address;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.exception.AddressInactiveException;
import com.javacore.spring_api_luvine.user.domain.exception.AddressNotFoundException;
import com.javacore.spring_api_luvine.user.domain.exception.UserSessionInvalidException;
import com.javacore.spring_api_luvine.user.domain.valueObject.Cep;
import com.javacore.spring_api_luvine.user.domain.valueObject.Name;
import com.javacore.spring_api_luvine.user.domain.valueObject.Phone;
import com.javacore.spring_api_luvine.user.dto.AddressRequest;
import com.javacore.spring_api_luvine.user.dto.AddressResponse;
import com.javacore.spring_api_luvine.user.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.mapper.UserMapper;
import com.javacore.spring_api_luvine.user.repository.AddressRepository;
import com.javacore.spring_api_luvine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public AddressResponse createAddress(CurrentUser currentUser, AddressRequest request) {
        Name firstName = new Name(request.firstName());
        Name lastName = new Name(request.lastName());
        Phone phone = new Phone(request.phone());
        Cep cep = new Cep(request.cep());

        User user = userRepository.findByPublicId(currentUser.publicId())
                .orElseThrow(UserSessionInvalidException::new);

        boolean hasNoDefault = !addressRepository.existsByUserPublicIdAndActiveTrue(currentUser.publicId());

        boolean shouldBeDefault = hasNoDefault || request.defaultAddress();

        if (shouldBeDefault) {
            addressRepository.resetDefaultAddressForUser(currentUser.publicId());
        }

        Address address = Address.create(
                user,
                firstName,
                lastName,
                cep,
                request.street(),
                request.number(),
                request.complement(),
                request.neighborhood(),
                request.city(),
                request.state(),
                request.country(),
                phone,
                shouldBeDefault
        );

        addressRepository.save(address);

        return userMapper.toAddressResponse(address);
    }

    @Transactional
    public MessageResponse deleteAddress(CurrentUser user, UUID addressPublicId) {
        Address address = getOwnedAddress(user.publicId(), addressPublicId);
        boolean wasDefault = address.isDefaultAddress();

        address.disable();

        if (wasDefault) {
            addressRepository.findFirstByUserPublicIdAndActiveTrueOrderByCreatedAtDesc(user.publicId())
                    .ifPresent(Address::markAsDefault);
        }

        return new MessageResponse("Endereço deletado com sucesso!");
    }

    @Transactional
    public AddressResponse setDefaultAddress(CurrentUser user, UUID addressPublicId) {
        Address newDefault = getOwnedAddress(user.publicId(), addressPublicId);

        if (!newDefault.isActive()) {
            throw new AddressInactiveException();
        }

        addressRepository.resetDefaultAddressForUser(user.publicId());

        newDefault.markAsDefault();
        return userMapper.toAddressResponse(newDefault);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> findAllAddresses(CurrentUser user) {
        return addressRepository.findAllByUserPublicIdAndActiveTrue(user.publicId())
                .stream()
                .map(userMapper::toAddressResponse)
                .toList();
    }

    private Address getOwnedAddress(UUID userPublicId, UUID addressPublicId) {
        return addressRepository.findByPublicIdAndUserPublicId(addressPublicId, userPublicId)
                .orElseThrow(AddressNotFoundException::new);
    }
}