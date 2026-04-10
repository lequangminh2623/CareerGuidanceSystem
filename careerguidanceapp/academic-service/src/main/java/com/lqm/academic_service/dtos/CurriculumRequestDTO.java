package com.lqm.academic_service.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CurriculumRequestDTO(
        UUID id,

        @NotNull(message = "{grade.id.notNull}") UUID gradeId,

        @NotNull(message = "{semester.id.notNull}") UUID semesterId,

        @NotNull(message = "{subject.id.notNull}") UUID subjectId) {
}
