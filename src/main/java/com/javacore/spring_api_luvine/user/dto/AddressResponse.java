package com.javacore.spring_api_luvine.user.dto;

import java.util.UUID;

public record AddressResponse(
        UUID publicId,
        String firstName,
        String lastName,
        String cep,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String country,
        String phone,
        boolean defaultAddress
) {
}