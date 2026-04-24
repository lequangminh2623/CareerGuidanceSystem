package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.configs.GradeConfig;
import com.lqm.academic_service.configs.SemesterConfig;
import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.models.Grade;
import com.lqm.academic_service.models.Semester;
import com.lqm.academic_service.models.Year;
import com.lqm.academic_service.repositories.YearRepository;
import com.lqm.academic_service.services.GradeService;
import com.lqm.academic_service.services.SemesterService;
import com.lqm.academic_service.services.YearService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class YearServiceImpl implements YearService {

    private final SemesterConfig semesterConfig;
    private final GradeConfig gradeConfig;
    private final YearRepository yearRepo;
    private final SemesterService semesterService;
    private final GradeService gradeService;
    private final MessageSource messageSource;

    @Override
    @Cacheable(value = "academic::years",
            key = "'all_' + #params?.getOrDefault('kw', '') + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Year> getYears(Map<String, String> params, Pageable pageable) {
        String kw = (params != null) ? params.get("kw") : null;
        return yearRepo.findAllByKeyword(kw, pageable);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "academic::years", allEntries = true),
            @CacheEvict(value = "academic::semesters", allEntries = true),
            @CacheEvict(value = "academic::grades", allEntries = true),
            @CacheEvict(value = "academic::curriculums", allEntries = true)
    })
    public Year saveYear(Year year) {
        boolean isNew = (year.getId() == null);
        Year savedYear = yearRepo.save(year);
        if (isNew && savedYear.getId() != null) {
            semesterConfig.getRequiredSemesters().forEach(semesterType -> semesterService.saveSemester(
                    Semester.builder().name(semesterType).build(), savedYear)
            );
            gradeConfig.getRequiredGrades().forEach(gradeType -> gradeService.saveGrade(
                    Grade.builder().name(gradeType).build(), savedYear)
            );
        }

        return savedYear;
    }

    @Override
    @Cacheable(value = "academic::years", key = "'id_' + #id")
    public Year getYearById(UUID id) {
        return yearRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                messageSource.getMessage("year.notFound", null, Locale.getDefault()))
        );
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "academic::years", allEntries = true),
            @CacheEvict(value = "academic::semesters", allEntries = true),
            @CacheEvict(value = "academic::grades", allEntries = true),
            @CacheEvict(value = "academic::curriculums", allEntries = true)
    })
    public void deleteYearById(UUID id) {
        yearRepo.deleteById(id);
    }

    @Override
    public boolean existDuplicateYear(String name, UUID excludeId) {
        return yearRepo.existsByNameAndIdNot(name, excludeId);
    }

    @Override
    public boolean existYearById(UUID yearId) {
        return yearRepo.existsById(yearId);
    }
}
