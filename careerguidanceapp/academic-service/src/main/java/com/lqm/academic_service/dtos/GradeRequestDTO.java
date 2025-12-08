package com.lqm.academic_service.dtos;

import com.lqm.academic_service.annotations.EnumValue;
import com.lqm.academic_service.models.GradeType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record GradeRequestDTO(
        UUID id,

        @EnumValue(enumClass = GradeType.class, message = "grade.name.invalid")
        String name,

        UUID yearId
) {}