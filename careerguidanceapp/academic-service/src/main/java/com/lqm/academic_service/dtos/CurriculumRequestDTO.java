package com.lqm.academic_service.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CurriculumRequestDTO(
        UUID id,

        @NotNull
        UUID gradeId,

        @NotNull
        UUID semesterId,

        @NotNull
        UUID subjectId
) {}
