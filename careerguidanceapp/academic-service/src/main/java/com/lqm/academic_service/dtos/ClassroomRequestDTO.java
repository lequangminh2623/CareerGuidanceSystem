package com.lqm.academic_service.dtos;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ClassroomRequestDTO(
        UUID id,

        String name,

        UUID gradeId,

        List<UUID> studentIds
) {}
