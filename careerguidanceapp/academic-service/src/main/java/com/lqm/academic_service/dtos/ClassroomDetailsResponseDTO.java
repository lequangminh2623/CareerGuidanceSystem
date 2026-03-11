package com.lqm.academic_service.dtos;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ClassroomDetailsResponseDTO(
        UUID id,
        String name,
        String gradeName,
        String yearName,
        List<UUID> studentIds
){}
