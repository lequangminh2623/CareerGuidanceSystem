package com.lqm.user_service.dtos;

import lombok.Builder;
import java.util.UUID;

@Builder
public record UserResponseDTO(
        UUID id,
        String firstName,
        String lastName,
        String avatar,
        String code
) {}
