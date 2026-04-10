package com.lqm.score_service.dtos;

import lombok.Builder;

import java.util.UUID;

@Builder
public record SectionResponseDTO(
                UUID id,
                UUID classroomId,
                String teacherName,
                String classroomName,
                String gradeName,
                String yearName,
                String semesterName,
                String subjectName,
                String scoreStatus) {
}
