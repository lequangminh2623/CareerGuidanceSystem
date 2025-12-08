package com.lqm.academic_service.dtos;

import com.lqm.academic_service.annotations.EnumValue;
import com.lqm.academic_service.models.SemesterType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record SemesterRequestDTO(
        UUID id,

        @EnumValue(enumClass = SemesterType.class, message = "semester.name.invalid")
        String name,

        UUID yearId
) {}
