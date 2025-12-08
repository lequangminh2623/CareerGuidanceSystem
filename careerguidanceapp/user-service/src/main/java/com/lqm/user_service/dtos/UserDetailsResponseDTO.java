package com.lqm.user_service.dtos;

import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

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
