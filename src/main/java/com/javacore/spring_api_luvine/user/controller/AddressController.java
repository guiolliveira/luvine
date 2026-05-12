package com.javacore.spring_api_luvine.user.controller;

import com.javacore.spring_api_luvine.shared.dto.MessageResponse;
import com.javacore.spring_api_luvine.user.dto.AddressRequest;
import com.javacore.spring_api_luvine.user.dto.AddressResponse;
import com.javacore.spring_api_luvine.user.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> create(
            @AuthenticationPrincipal CurrentUser user, @RequestBody @Valid AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(user, request));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> findAllAddresses(@AuthenticationPrincipal CurrentUser user) {
        return ResponseEntity.ok(addressService.findAllAddresses(user));
    }

    @PatchMapping("/{publicId}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @AuthenticationPrincipal CurrentUser user, @PathVariable UUID publicId) {
        return ResponseEntity.ok(addressService.setDefaultAddress(user, publicId));
    }

    @DeleteMapping("/{publicId}/delete")
    public ResponseEntity<MessageResponse> delete(
            @AuthenticationPrincipal CurrentUser user, @PathVariable UUID publicId) {
        return ResponseEntity.ok(addressService.deleteAddress(user, publicId));
    }
}