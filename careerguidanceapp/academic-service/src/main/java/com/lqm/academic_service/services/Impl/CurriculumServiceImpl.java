package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.configs.SubjectConfig;
import com.lqm.academic_service.models.Curriculum;
import com.lqm.academic_service.models.Grade;
import com.lqm.academic_service.models.Semester;
import com.lqm.academic_service.models.Subject;
import com.lqm.academic_service.repositories.CurriculumRepository;
import com.lqm.academic_service.services.CurriculumService;
import com.lqm.academic_service.services.GradeService;
import com.lqm.academic_service.services.SemesterService;
import com.lqm.academic_service.services.SubjectService;
import com.lqm.academic_service.specifications.CurriculumSpecification;
import com.lqm.academic_service.specifications.SectionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CurriculumServiceImpl implements CurriculumService {

    private final CurriculumRepository curriculumRepo;
    private final MessageSource messageSource;

    @Override
    public Page<Curriculum> getCurriculums(List<UUID> ids, Map<String, String> params, Pageable pageable) {
        Specification<Curriculum> spec = CurriculumSpecification.filterByParams(params).and(CurriculumSpecification.hasIdIn(ids));
        return curriculumRepo.findAll(spec, pageable);
    }

    @Override
    public Curriculum getCurriculumById(UUID id) {
        return curriculumRepo.findById(id).orElseThrow(() -> new RuntimeException(
                messageSource.getMessage("curriculum.notFound", null, Locale.getDefault())
        ));
    }

    @Override
    public Curriculum saveCurriculum(Curriculum entity, Grade grade, Semester semester, Subject subject) {
        entity.setGrade(grade);
        entity.setSemester(semester);
        entity.setSubject(subject);

        return curriculumRepo.save(entity);
    }

    @Override
    public void deleteCurriculum(UUID id) {
        curriculumRepo.deleteById(id);
    }

    @Override
    public boolean existDuplicateCurriculum(UUID gradeId, UUID semesterId, UUID subjectId, UUID excludedId) {
        return curriculumRepo.existsByGradeIdAndSemesterIdAndSubjectIdAndIdNot(gradeId, semesterId, subjectId, excludedId);
    }
}
