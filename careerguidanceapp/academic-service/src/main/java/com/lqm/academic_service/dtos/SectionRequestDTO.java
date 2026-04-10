package com.lqm.academic_service.dtos;

import com.lqm.academic_service.annotations.EnumValue;
import com.lqm.academic_service.models.ScoreStatusType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record SectionRequestDTO(
        UUID id,

        UUID teacherId,

        @NotNull(message = "{classroom.id.notNull}") UUID classroomId,

        @NotNull(message = "{curriculum.id.notNull}") UUID curriculumId,

        @EnumValue(enumClass = ScoreStatusType.class, message = "{section.scoreStatus.invalid}") String scoreStatus) {
}
