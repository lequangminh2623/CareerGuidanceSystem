package com.lqm.admin_service.dtos;

import com.lqm.admin_service.annotations.EnumValue;
import com.lqm.admin_service.models.ScoreStatusType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ChangeStatusRequestDTO(
        @NotNull(message = "Status cannot be null")
        @EnumValue(enumClass = ScoreStatusType.class, message = "section.scoreStatus.invalid")
        String status
) {}
