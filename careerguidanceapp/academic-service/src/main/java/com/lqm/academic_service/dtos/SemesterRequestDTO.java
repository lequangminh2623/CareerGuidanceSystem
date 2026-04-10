package com.lqm.academic_service.dtos;

import com.lqm.academic_service.annotations.EnumValue;
import com.lqm.academic_service.models.SemesterType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record SemesterRequestDTO(
                UUID id,

                @EnumValue(enumClass = SemesterType.class, message = "{semester.name.invalid}") String name,

                @NotNull(message = "{semester.year.notNull}") UUID yearId) {
}
