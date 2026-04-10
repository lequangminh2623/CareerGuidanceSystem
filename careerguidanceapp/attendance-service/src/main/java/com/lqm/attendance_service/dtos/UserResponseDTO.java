package com.lqm.attendance_service.dtos;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserResponseDTO(
        UUID id,
        String code,
        String firstName,
        String lastName
) {}

