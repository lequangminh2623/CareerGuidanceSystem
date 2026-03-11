package com.lqm.admin_service.dtos;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.util.UUID;

@Builder
public record YearRequestDTO(
        UUID id,
        @Pattern(regexp = "^[0-9]{4}-[0-9]{4}$", message = "year.name.invalid")
        String name
) {}
