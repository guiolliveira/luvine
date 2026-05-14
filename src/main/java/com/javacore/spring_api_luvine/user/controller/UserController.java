package com.javacore.spring_api_luvine.user.controller;

import com.javacore.spring_api_luvine.user.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.dto.ProfileResponse;
import com.javacore.spring_api_luvine.user.dto.UpdateProfileRequest;
import com.javacore.spring_api_luvine.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ProfileResponse> profile(@AuthenticationPrincipal CurrentUser currentUser) {
        ProfileResponse profileResponse = userService.findProfile(currentUser);
        return ResponseEntity.ok(profileResponse);
    }

    @PostMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestBody @Valid UpdateProfileRequest request) {
        userService.updateProfile(currentUser, request);
        return ResponseEntity.noContent().build();
    }
}