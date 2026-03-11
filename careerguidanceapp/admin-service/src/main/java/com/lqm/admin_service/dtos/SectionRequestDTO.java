package com.lqm.admin_service.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SectionRequestDTO(
        UUID id,

        UUID teacherId,

        @NotNull
        UUID classroomId,

        @NotNull
        UUID curriculumId,

        @NotNull
        String scoreStatus
) {}
