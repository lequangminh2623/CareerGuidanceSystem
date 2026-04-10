package com.lqm.score_service.services;

import com.lqm.score_service.dtos.*;

import java.util.List;
import java.util.UUID;

public interface StatisticsService {

    StudentStatisticsResponseDTO getStudentStatistics(UUID studentId);

    List<TeacherSectionAvgDTO> getTeacherSectionStatistics(UUID teacherId, String yearName);

    List<TeacherGradeStatisticsDTO> getTeacherGradeStatistics(UUID teacherId, String subjectName);

    List<SubjectResponseDTO> getAllSubjects();
}
