package com.lqm.academic_service.services;

import com.lqm.academic_service.models.Curriculum;
import com.lqm.academic_service.models.Grade;
import com.lqm.academic_service.models.Semester;
import com.lqm.academic_service.models.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CurriculumService {
    Page<Curriculum> getCurriculumsByIds(List<UUID> ids, Map<String, String> params, Pageable pageable);

    Page<Curriculum> getCurriculums(Map<String, String> params, Pageable pageable);

    Curriculum getCurriculumById(UUID id);

    Curriculum saveCurriculum(Curriculum entity, Grade grade, Semester semester, Subject subject);

    void deleteCurriculum(UUID id);

    boolean existDuplicateCurriculum(UUID uuid, UUID uuid1, UUID uuid2, UUID excludedId);
}
