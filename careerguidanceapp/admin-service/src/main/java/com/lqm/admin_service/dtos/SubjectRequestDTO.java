package com.lqm.admin_service.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.UUID;

@Builder
public record SubjectRequestDTO(
        UUID id,

        @NotBlank(message = "{subject.name.notNull}")
        String name
) {}
