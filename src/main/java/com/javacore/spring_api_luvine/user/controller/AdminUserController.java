package com.javacore.spring_api_luvine.user.controller;

import com.javacore.spring_api_luvine.user.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.dto.UpdateRoleRequest;
import com.javacore.spring_api_luvine.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @PostMapping("/{publicId}/role")
    public ResponseEntity<Void> updateRole(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID publicId,
            @RequestBody UpdateRoleRequest request) {
        userService.updateRole(currentUser, publicId, request);
        return ResponseEntity.noContent().build();
    }
}