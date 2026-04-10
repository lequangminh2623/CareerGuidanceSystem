package com.lqm.academic_service.dtos;

import lombok.Builder;

@Builder
public record UserMessageResponseDTO(
        String firstName,
        String lastName,
        String email,
        String avatar,
        String role
) {}
