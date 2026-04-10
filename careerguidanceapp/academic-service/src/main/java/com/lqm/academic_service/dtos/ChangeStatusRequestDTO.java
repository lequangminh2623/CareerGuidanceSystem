package com.lqm.academic_service.dtos;

import com.lqm.academic_service.annotations.EnumValue;
import com.lqm.academic_service.models.ScoreStatusType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ChangeStatusRequestDTO(
        @NotNull(message = "{section.status.notNull}") @EnumValue(enumClass = ScoreStatusType.class, message = "{section.scoreStatus.invalid}") String status) {
}
