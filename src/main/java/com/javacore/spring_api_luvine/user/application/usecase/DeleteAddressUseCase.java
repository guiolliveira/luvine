package com.javacore.spring_api_luvine.user.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.mapper.UserMapper;
import com.javacore.spring_api_luvine.user.domain.entity.Address;
import com.javacore.spring_api_luvine.user.domain.exception.AddressNotFoundException;
import com.javacore.spring_api_luvine.user.infrastructure.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class DeleteAddressUseCase {

    private final AddressRepository addressRepository;


    @Transactional
    public void execute(CurrentUser currentUser, UUID addressPublicId) {
        log.info("event=delete_address_attempt publicId={} addressPublicId={}",
                currentUser.publicId(), addressPublicId);

        Address address =  addressRepository.findByPublicIdAndUserPublicId(addressPublicId, currentUser.publicId())
                .orElseThrow(() -> {
                    log.warn("event=address_not_found publicId={} addressPublicId={}",
                            currentUser.publicId(), addressPublicId);
                    return new AddressNotFoundException();
                });

        boolean wasDefault = address.isDefaultAddress();

        address.disable();

        if (wasDefault) {
            addressRepository.findFirstByUserPublicIdAndActiveTrueOrderByCreatedAtDesc(currentUser.publicId())
                    .ifPresent(next -> {
                        next.markAsDefault();
                        log.info("event=default_address_reassigned publicId={} newDefaultAddressPublicId={}",
                                currentUser.publicId(), next.getPublicId());
                    });
        }

        log.info("event=address_deleted publicId={} addressPublicId={} wasDefault={}",
                currentUser.publicId(), addressPublicId, wasDefault);
    }
}