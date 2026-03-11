package com.lqm.admin_service.dtos;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserDetailsResponseDTO(
        UUID id,
        String firstName,
        String lastName,
        boolean gender,
        String email,
        String avatar,
        LocalDateTime createdDate,
        LocalDateTime updatedDate,
        String role,
        String code,
        boolean active
){}
