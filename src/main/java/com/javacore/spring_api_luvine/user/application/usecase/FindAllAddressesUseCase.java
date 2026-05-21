package com.javacore.spring_api_luvine.user.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.user.application.dto.AddressResponse;
import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.mapper.UserMapper;
import com.javacore.spring_api_luvine.user.infrastructure.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class FindAllAddressesUseCase {

    private final AddressRepository addressRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<AddressResponse> execute(CurrentUser currentUser) {
        log.debug("event=find_all_addresses publicId={}", currentUser.publicId());

        List<AddressResponse> addresses = addressRepository.findAllByUserPublicIdAndActiveTrue(currentUser.publicId())
                .stream()
                .map(userMapper::toAddressResponse)
                .toList();

        log.debug("event=find_all_addresses_completed publicId={} count={}", currentUser.publicId(), addresses.size());

        return addresses;
    }
}