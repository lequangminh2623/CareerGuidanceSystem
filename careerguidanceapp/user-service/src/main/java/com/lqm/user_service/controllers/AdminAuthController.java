package com.lqm.user_service.controllers;

import com.lqm.user_service.dtos.AdminUserLoginDTO;
import com.lqm.user_service.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.lqm.user_service.exceptions.ResourceNotFoundException;

@RestController
@RequestMapping("/api/internal/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final UserService userService;

    @GetMapping("/{email}")
    public ResponseEntity<AdminUserLoginDTO> getUserForAuth(@PathVariable String email) {
        UserDetails user;
        try {
            user = userService.loadUserByUsername(email);
        } catch (UsernameNotFoundException ex) {
            throw new ResourceNotFoundException("User not found: " + email);
        }

        String role = user.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("STUDENT");

        return ResponseEntity.ok(AdminUserLoginDTO.builder()
                .email(user.getUsername())
                .password(user.getPassword())
                .role(role.replace("ROLE_", ""))
                .build());
    }
}
