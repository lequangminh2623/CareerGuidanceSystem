package com.lqm.admin_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record GradeRequestDTO(
        UUID id,

        @NotBlank(message = "{grade.name.notNull}")
        String name,

        @NotNull(message = "{grade.year.notNull}")
        UUID yearId
) {}