package com.javacore.spring_api_luvine.user.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
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
import com.javacore.spring_api_luvine.user.domain.valueObject.Phone;
import com.javacore.spring_api_luvine.user.infrastructure.repository.AddressRepository;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class CreateAddressUseCase {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public AddressResponse execute(CurrentUser currentUser, AddressRequest request) {
        log.info("event=create_address_attempt publicId={}", currentUser.publicId());

        PersonName firstName = new PersonName(request.firstName());
        PersonName lastName = new PersonName(request.lastName());
        Phone phone = new Phone(request.phone());
        Cep cep = new Cep(request.cep());

        User user = userRepository.findByPublicId(currentUser.publicId())
                .orElseThrow(UserSessionInvalidException::new);

        boolean alreadyExists = addressRepository
                .existsByUserPublicIdAndFirstNameAndLastNameAndCepAndNumberAndComplementAndActiveTrue(
                        currentUser.publicId(),
                        firstName,
                        lastName,
                        cep,
                        request.number(),
                        request.complement()
                );

        if (alreadyExists) {
            log.warn("event=create_address_rejected reason=address_already_exists publicId={}", currentUser.publicId());
            throw new AddressAlreadyExistsException();
        }

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

        log.info("event=address_created publicId={} addressPublicId={} default={}",
                currentUser.publicId(), address.getPublicId(), shouldBeDefault);

        return userMapper.toAddressResponse(address);
    }
}