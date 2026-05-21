package com.javacore.spring_api_luvine.user.application.service;

import com.javacore.spring_api_luvine.user.domain.entity.Address;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.exception.AddressAlreadyExistsException;
import com.javacore.spring_api_luvine.user.domain.exception.AddressInactiveException;
import com.javacore.spring_api_luvine.user.domain.exception.AddressNotFoundException;
import com.javacore.spring_api_luvine.user.domain.exception.UserSessionInvalidException;
import com.javacore.spring_api_luvine.user.domain.valueObject.Cep;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
import com.javacore.spring_api_luvine.user.domain.valueObject.Phone;
import com.javacore.spring_api_luvine.user.application.dto.AddressRequest;
import com.javacore.spring_api_luvine.user.application.dto.AddressResponse;
import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.mapper.UserMapper;
import com.javacore.spring_api_luvine.user.infrastructure.repository.AddressRepository;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserMapper userMapper;

    @Transactional
    public AddressResponse setDefaultAddress(CurrentUser user, UUID addressPublicId) {
        log.info("event=set_default_address_attempt publicId={} addressPublicId={}", user.publicId(), addressPublicId);

        Address newDefault = getOwnedAddress(user.publicId(), addressPublicId);

        if (!newDefault.isActive()) {
            log.warn("event=set_default_address_rejected reason=address_inactive publicId={} addressPublicId={}",
                    user.publicId(), addressPublicId);
            throw new AddressInactiveException();
        }

        addressRepository.resetDefaultAddressForUser(user.publicId());
        newDefault.markAsDefault();

        log.info("event=default_address_updated publicId={} addressPublicId={}", user.publicId(), addressPublicId);

        return userMapper.toAddressResponse(newDefault);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> findAllAddresses(CurrentUser user) {
        log.debug("event=find_all_addresses publicId={}", user.publicId());

        List<AddressResponse> addresses = addressRepository.findAllByUserPublicIdAndActiveTrue(user.publicId())
                .stream()
                .map(userMapper::toAddressResponse)
                .toList();

        log.debug("event=find_all_addresses_completed publicId={} count={}", user.publicId(), addresses.size());

        return addresses;
    }

    private Address getOwnedAddress(UUID userPublicId, UUID addressPublicId) {
        return addressRepository.findByPublicIdAndUserPublicId(addressPublicId, userPublicId)
                .orElseThrow(() -> {
                    log.warn("event=address_not_found publicId={} addressPublicId={}", userPublicId, addressPublicId);
                    return new AddressNotFoundException();
                });
    }
}