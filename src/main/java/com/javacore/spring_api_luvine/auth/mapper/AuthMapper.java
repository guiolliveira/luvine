package com.javacore.spring_api_luvine.auth.mapper;

import com.javacore.spring_api_luvine.auth.dto.RegisterResponse;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "firstName", source = "firstName.value")
    @Mapping(target = "lastName", source = "lastName.value")
    RegisterResponse toRegisterResponse(User user);
}