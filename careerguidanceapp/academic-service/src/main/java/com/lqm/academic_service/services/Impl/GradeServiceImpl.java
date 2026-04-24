package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.configs.SubjectConfig;
import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.models.*;
import com.lqm.academic_service.repositories.CurriculumRepository;
import com.lqm.academic_service.repositories.GradeRepository;
import com.lqm.academic_service.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepo;
    private final MessageSource messageSource;
    private final CurriculumRepository curriculumRepo;
    private final SemesterService semesterService;
    private final SubjectConfig subjectConfig;
    private final SubjectService subjectService;

    @Override
    @Cacheable(value = "academic::grades",
            key = "'yearId_' + #id + '_' + #params?.getOrDefault('kw', '')")
    public List<Grade> getGradesByYearId(UUID id, Map<String, String> params) {
        String kw = (params != null) ? params.get("kw") : null;
        return gradeRepo.findByYearId(id, kw);
    }

    @Override
    @Cacheable(value = "academic::grades",
            key = "'all_' + #params?.getOrDefault('kw', '')")
    public List<Grade> getGrades(Map<String, String> params) {
        String kw = (params != null) ? params.get("kw") : null;
        return gradeRepo.findAllByKeyword(kw);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "academic::grades", allEntries = true),
            @CacheEvict(value = "academic::curriculums", allEntries = true)
    })
    public Grade saveGrade(Grade grade, Year year) {
        boolean isNew = (grade.getId() == null);
        grade.setYear(year);
        Grade savedgrade = gradeRepo.save(grade);
        if (isNew && savedgrade.getId() != null) {
            this.initCurriculumForGrade(savedgrade);
        }

        return savedgrade;
    }

    private void initCurriculumForGrade(Grade grade) {
        List<Semester> semesters = semesterService.getSemestersByYearId(grade.getYear().getId(), Map.of());
        List<Curriculum> curriculumList = new ArrayList<>();
        subjectConfig.getRequiredSubjects().forEach(subjectName -> {
            semesters.forEach(semester ->
                    curriculumList.add(
                            Curriculum.builder()
                                    .grade(grade)
                                    .subject(subjectService.getSubjectByName(subjectName))
                                    .semester(semester)
                                    .build()
                    )
            );
        });

        curriculumRepo.saveAll(curriculumList);
    }

    @Override
    @Cacheable(value = "academic::grades", key = "'id_' + #id")
    public Grade getGradeById(UUID id) {
        return gradeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                messageSource.getMessage("grade.notFound", null, Locale.getDefault()))
        );
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "academic::grades", allEntries = true),
            @CacheEvict(value = "academic::curriculums", allEntries = true)
    })
    public void deleteGradeById(UUID id) {
        gradeRepo.deleteById(id);
    }

    @Override
    public boolean existDuplicateGrade(GradeType name, UUID gradeId, UUID yearId) {
            return gradeRepo.existsByNameAndYearIdAndIdNot(name, yearId, gradeId);
    }

    @Override
    public boolean existGradeById(UUID gradeId) {
        return gradeRepo.existsById(gradeId);
    }
}
