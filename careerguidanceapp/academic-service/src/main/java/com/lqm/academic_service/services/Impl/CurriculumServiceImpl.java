package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.events.ChatGroupDeleteEvent;
import com.lqm.academic_service.events.ScoreSyncEvent;
import com.lqm.academic_service.models.Curriculum;
import com.lqm.academic_service.models.Grade;
import com.lqm.academic_service.models.Section;
import com.lqm.academic_service.models.Semester;
import com.lqm.academic_service.models.Subject;
import com.lqm.academic_service.repositories.CurriculumRepository;
import com.lqm.academic_service.repositories.SectionRepository;
import com.lqm.academic_service.services.AcademicEventPublisher;
import com.lqm.academic_service.services.CurriculumService;
import com.lqm.academic_service.specifications.CurriculumSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class CurriculumServiceImpl implements CurriculumService {

    private final CurriculumRepository curriculumRepo;
    private final MessageSource messageSource;
    private final SectionRepository sectionRepo;
    private final AcademicEventPublisher eventPublisher;

    @Override
    public Page<Curriculum> getCurriculumsByIds(List<UUID> ids, Map<String, String> params, Pageable pageable) {
        Specification<Curriculum> spec = CurriculumSpecification.filterByParams(params).and(CurriculumSpecification.hasIdIn(ids));
        return curriculumRepo.findAll(spec, pageable);
    }

    @Override
    @Cacheable(value = "academic::curriculums",
            condition = "#pageable.isPaged()",
            key = "'all_' + #params?.getOrDefault('gradeId', '') + '_' + #params?.getOrDefault('semesterId', '') + '_' + #params?.getOrDefault('subjectId', '') + '_' + #params?.getOrDefault('yearId', '') + '_' + (#pageable.isPaged() ? #pageable.pageNumber : 'unpaged') + '_' + (#pageable.isPaged() ? #pageable.pageSize : 'unpaged')")
    public Page<Curriculum> getCurriculums(Map<String, String> params, Pageable pageable) {
        Specification<Curriculum> spec = CurriculumSpecification.filterByParams(params);
        return curriculumRepo.findAll(spec, pageable);
    }

    @Override
    @Cacheable(value = "academic::curriculums", key = "'id_' + #id")
    public Curriculum getCurriculumById(UUID id) {
        return curriculumRepo.findById(id).orElseThrow(() -> new RuntimeException(
                messageSource.getMessage("curriculum.notFound", null, Locale.getDefault())
        ));
    }

    @Override
    @CacheEvict(value = "academic::curriculums", allEntries = true)
    public Curriculum saveCurriculum(Curriculum entity, Grade grade, Semester semester, Subject subject) {
        entity.setGrade(grade);
        entity.setSemester(semester);
        entity.setSubject(subject);

        return curriculumRepo.save(entity);
    }

    @Override
    @CacheEvict(value = "academic::curriculums", allEntries = true)
    public void deleteCurriculum(UUID id) {
        // Fetch sections associated with this curriculum to delete their chat groups and scores
        List<Section> sections = sectionRepo.findByCurriculumId(id);
        List<UUID> sectionIds = sections.stream().map(Section::getId).toList();

        // Explicitly delete sections via JPA to avoid TransientPropertyValueException on flush
        if (!sections.isEmpty()) {
            sectionRepo.deleteAll(sections);
        }

        // Delete the curriculum itself
        curriculumRepo.deleteById(id);

        if (!sectionIds.isEmpty()) {
            eventPublisher.publishScoreSync(new ScoreSyncEvent(sectionIds, Collections.emptyList(), Collections.emptyList()));
            eventPublisher.publishChatGroupDelete(new ChatGroupDeleteEvent(sectionIds));
        }
    }

    @Override
    public boolean existDuplicateCurriculum(UUID gradeId, UUID semesterId, UUID subjectId, UUID excludedId) {
        return curriculumRepo.existsByGradeIdAndSemesterIdAndSubjectIdAndIdNot(gradeId, semesterId, subjectId, excludedId);
    }
}
