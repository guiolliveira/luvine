package com.javacore.spring_api_luvine.user.domain.entity;

import lombok.Getter;

import java.util.List;

@Getter
public enum UserRole {
    CUSTOMER(0, List.of(Authority.ROLE_CUSTOMER)),
    ADMIN(1, List.of(Authority.ROLE_CUSTOMER, Authority.ROLE_ADMIN)),
    SUPER_ADMIN(2, List.of(Authority.ROLE_SUPER_ADMIN, Authority.ROLE_ADMIN, Authority.ROLE_CUSTOMER));

    private final int hierarchy;
    private final List<Authority> authorities;

    UserRole(int hierarchy, List<Authority> authorities) {
        this.hierarchy = hierarchy;
        this.authorities = authorities;
    }

    private boolean hasHigherAuthorityTan(UserRole other) {
        return this.hierarchy > other.hierarchy;
    }
}