package com.lqm.academic_service.services;

import com.lqm.academic_service.models.Grade;
import com.lqm.academic_service.models.GradeType;
import com.lqm.academic_service.models.Year;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GradeService {

    List<Grade> getGradesByYearId(UUID year_id, Map<String, String> params);

    List<Grade> getGrades(Map<String, String> params);

    Grade saveGrade(Grade Grade, Year year);

    Grade getGradeById(UUID id);

    void deleteGradeById(UUID id);

    boolean existDuplicateGrade(GradeType name, UUID id, UUID yearId);

    boolean existGradeById(UUID gradeId);
}