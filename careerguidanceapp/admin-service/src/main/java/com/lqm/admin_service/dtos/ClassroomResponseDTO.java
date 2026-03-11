package com.lqm.admin_service.dtos;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ClassroomResponseDTO(
        UUID id,
        String name,
        Integer studentCount
){}
