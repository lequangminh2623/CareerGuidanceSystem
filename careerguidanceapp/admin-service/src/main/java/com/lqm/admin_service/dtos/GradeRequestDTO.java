package com.lqm.admin_service.dtos;

import lombok.Builder;

import java.util.UUID;

@Builder
public record GradeRequestDTO(
        UUID id,
        String name,
        UUID yearId
) {}