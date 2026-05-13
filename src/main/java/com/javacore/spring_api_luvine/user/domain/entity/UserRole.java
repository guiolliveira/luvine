package com.javacore.spring_api_luvine.user.domain.entity;

import lombok.Getter;

import java.util.List;

@Getter
public enum UserRole {
    CUSTOMER(List.of(Authority.ROLE_CUSTOMER)),
    ADMIN(List.of(Authority.ROLE_CUSTOMER, Authority.ROLE_ADMIN));

    private final List<Authority> authorities;

    UserRole(List<Authority> authorities) {
        this.authorities = authorities;
    }
}