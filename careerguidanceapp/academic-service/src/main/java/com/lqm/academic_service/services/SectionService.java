package com.lqm.academic_service.services;

import com.lqm.academic_service.models.ScoreStatusType;
import com.lqm.academic_service.models.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public interface SectionService {

    Page<Section> getSections(List<UUID> ids, Map<String, String> params, Pageable pageable);

    Section getSectionById(UUID id);

    void saveSections(Map<UUID, Section> curriculumSectionMap, UUID classroomId);

    void  changeScoreStatus(UUID id, ScoreStatusType scoreStatusType);

    void lockSection(UUID sectionId);

    void deleteSection(UUID id);

    boolean isLockedSection(UUID id);

    boolean existTeacherInSection(UUID teacherId, UUID sectionId);

    boolean existSectionById(UUID id);

    Map<UUID, UUID> getExistingCurriculumMap(UUID classroomId, Set<UUID> curriculumIds);

    Map<UUID, String> buildTeacherMap(List<Section> sections);

}
