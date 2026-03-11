package com.lqm.academic_service.dtos;

import java.util.UUID;

public record CurriculumResponseDTO(
        UUID id,
        String semesterName,
        String subjectName
) {}
