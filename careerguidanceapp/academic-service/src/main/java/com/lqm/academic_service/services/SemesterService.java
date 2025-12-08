package com.lqm.academic_service.services;

import com.lqm.academic_service.models.Semester;
import com.lqm.academic_service.models.SemesterType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SemesterService {

    List<Semester> getSemestersByYearId(UUID year_id, Map<String, String> params);

    Semester saveSemester(Semester semester, UUID yearId);

    Semester getSemesterById(UUID id);

    void deleteSemesterById(UUID id);

    boolean existDuplicateSemester(SemesterType name, UUID id, UUID yearId);
}
