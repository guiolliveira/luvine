package com.javacore.spring_api_luvine.user.application.dto;

import com.javacore.spring_api_luvine.user.domain.entity.UserRole;

public record UpdateRoleRequest(UserRole role) {
}