package com.lqm.score_service.dtos;

import lombok.Builder;

import java.util.UUID;

@Builder
public record SubjectResponseDTO(
        UUID id,
        String name
) {}
