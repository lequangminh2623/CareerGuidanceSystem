package com.lqm.academic_service.dtos;

import lombok.Builder;

import java.util.UUID;

@Builder
public record GradeDetailsResponseDTO(
        UUID id,
        String name,
        String yearName
) {}
