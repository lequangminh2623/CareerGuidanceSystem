package com.lqm.user_service.controllers;

import com.lqm.user_service.dtos.AdminUserLoginDTO;
import com.lqm.user_service.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/auth")
@CrossOrigin
@RequiredArgsConstructor
public class AdminAuthController {

    private final UserService userService;

    @GetMapping("/{email}")
    public ResponseEntity<AdminUserLoginDTO> getUserForAuth(@PathVariable String email) {
        UserDetails user = userService.loadUserByUsername(email);

        String role = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("STUDENT");

        return ResponseEntity.ok(AdminUserLoginDTO.builder()
                .email(user.getUsername())
                .password(user.getPassword())
                .role(role.replace("ROLE_", ""))
                .build()
        );
    }
}
