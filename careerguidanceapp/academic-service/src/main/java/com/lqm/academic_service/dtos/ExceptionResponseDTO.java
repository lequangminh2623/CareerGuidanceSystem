package com.lqm.academic_service.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExceptionResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Object details,
        String path
) {}
