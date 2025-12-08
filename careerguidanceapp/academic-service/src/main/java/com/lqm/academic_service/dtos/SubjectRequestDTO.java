package com.lqm.academic_service.dtos;

import lombok.Builder;

import java.util.UUID;

@Builder
public record SubjectRequestDTO(
        UUID id,
        String name
) {}
