package com.lqm.academic_service.dtos;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ClassroomResponseDTO(
        UUID id,
        String name,
        String gradeName,
        String yearName
){}
