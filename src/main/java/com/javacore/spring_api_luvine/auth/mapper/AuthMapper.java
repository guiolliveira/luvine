package com.javacore.spring_api_luvine.auth.mapper;

import com.javacore.spring_api_luvine.auth.dto.RegisterResponse;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    RegisterResponse toRegisterResponse(User user);
}