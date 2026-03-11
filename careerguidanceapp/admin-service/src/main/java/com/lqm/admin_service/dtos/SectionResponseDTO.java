package com.lqm.admin_service.dtos;

import java.util.UUID;

public record SectionResponseDTO(
        UUID id,
        UUID classroomId,
        String teacherName,
        String classroomName,
        String gradeName,
        String semesterName,
        String yearName,
        String subjectName,
        String scoreStatus
) {}
