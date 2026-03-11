package com.lqm.admin_service.dtos;

import lombok.Builder;

import java.util.UUID;

@Builder
public record SubjectRequestDTO(
        UUID id,
        String name
) {}
