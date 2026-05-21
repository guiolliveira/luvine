package com.javacore.spring_api_luvine.user.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.user.application.dto.AddressResponse;
import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.mapper.UserMapper;
import com.javacore.spring_api_luvine.user.domain.entity.Address;
import com.javacore.spring_api_luvine.user.domain.exception.AddressInactiveException;
import com.javacore.spring_api_luvine.user.domain.exception.AddressNotFoundException;
import com.javacore.spring_api_luvine.user.infrastructure.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SetDefaultAddressUseCase {

    private final AddressRepository addressRepository;
    private final UserMapper userMapper;

    @Transactional
    public AddressResponse execute(CurrentUser currentUser, UUID addressPublicId) {
        log.info("event=set_default_address_attempt publicId={} addressPublicId={}",
                currentUser.publicId(), addressPublicId);

        Address newDefault =  addressRepository.findByPublicIdAndUserPublicId(addressPublicId, currentUser.publicId())
                .orElseThrow(() -> {
                    log.warn("event=address_not_found publicId={} addressPublicId={}",
                            currentUser.publicId(), addressPublicId);
                    return new AddressNotFoundException();
                });

        if (!newDefault.isActive()) {
            log.warn("event=set_default_address_rejected reason=address_inactive publicId={} addressPublicId={}",
                    currentUser.publicId(), addressPublicId);
            throw new AddressInactiveException();
        }

        addressRepository.resetDefaultAddressForUser(currentUser.publicId());
        newDefault.markAsDefault();

        log.info("event=default_address_updated publicId={} addressPublicId={}",
                currentUser.publicId(), addressPublicId);

        return userMapper.toAddressResponse(newDefault);
    }
}