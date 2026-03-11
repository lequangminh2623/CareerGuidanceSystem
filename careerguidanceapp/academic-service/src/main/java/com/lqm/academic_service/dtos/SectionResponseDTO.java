package com.lqm.academic_service.dtos;


import java.util.UUID;

public record SectionResponseDTO(
        UUID id,
        UUID classroomId,
        String teacherName,
        String classroomName,
        String gradeName,
        String yearName,
        String semesterName,
        String subjectName,
        String scoreStatus
) {}
