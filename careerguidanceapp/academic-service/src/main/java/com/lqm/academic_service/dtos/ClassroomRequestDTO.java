package com.lqm.academic_service.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ClassroomRequestDTO(
        UUID id,

        String name,

        @NotNull(message = "classroom.grade.notNull")
        UUID gradeId,

        List<UUID> studentIds
) {}
