package com.lqm.admin_service.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SectionRequestDTO(
        UUID id,

        UUID teacherId,

        @NotNull(message = "{classroom.id.notNull}")
        UUID classroomId,

        @NotNull(message = "{curriculum.id.notNull}")
        UUID curriculumId,

        @NotNull(message = "{section.status.notNull}")
        String scoreStatus
) {}
