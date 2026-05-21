package com.javacore.spring_api_luvine.user.application.mapper;

import com.javacore.spring_api_luvine.user.domain.entity.Address;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.application.dto.AddressResponse;
import com.javacore.spring_api_luvine.user.application.dto.ProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "firstName", source = "firstName.value")
    @Mapping(target = "lastName", source = "lastName.value")
    @Mapping(target = "cep", expression = "java(address.getCep().getFormatted())")
    @Mapping(target = "phone", expression = "java(address.getPhone().getFormatted())")
    AddressResponse toAddressResponse(Address address);

    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "firstName", source = "firstName.value")
    @Mapping(target = "lastName", source = "lastName.value")
    @Mapping(target = "avatarInitial", expression = "java(user.getFirstName().value().substring(0, 1).toUpperCase())")
    ProfileResponse toProfileResponse(User user);
}