package com.javacore.spring_api_luvine.user.mapper;

import com.javacore.spring_api_luvine.user.domain.entity.Address;
import com.javacore.spring_api_luvine.user.dto.AddressResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "firstName", source = "firstName.value")
    @Mapping(target = "lastName", source = "lastName.value")
    @Mapping(target = "cep", expression = "java(address.getCep().getFormatted())")
    @Mapping(target = "phone", expression = "java(address.getPhone().getFormatted())")
    AddressResponse toAddressResponse(Address address);
}