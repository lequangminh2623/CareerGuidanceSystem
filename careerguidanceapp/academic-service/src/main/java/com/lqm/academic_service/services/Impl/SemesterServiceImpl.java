package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.models.Semester;
import com.lqm.academic_service.models.SemesterType;
import com.lqm.academic_service.models.Year;
import com.lqm.academic_service.repositories.SemesterRepository;
import com.lqm.academic_service.services.SemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
@RequiredArgsConstructor
@Service
public class SemesterServiceImpl implements SemesterService {

    private final SemesterRepository semesterRepo;
    private final MessageSource messageSource;

    @Override
    @Cacheable(value = "academic::semesters",
            key = "'yearId_' + #id + '_' + #params?.getOrDefault('kw', '')")
    public List<Semester> getSemestersByYearId(UUID id, Map<String, String> params) {
        String kw = (params != null) ? params.get("kw") : null;
        return semesterRepo.findByYearId(id, kw);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "academic::semesters", allEntries = true),
            @CacheEvict(value = "academic::curriculums", allEntries = true)
    })
    public Semester saveSemester(Semester semester, Year year) {
        semester.setYear(year);
        return semesterRepo.save(semester);
    }

    @Override
    @Cacheable(value = "academic::semesters", key = "'id_' + #id")
    public Semester getSemesterById(UUID id) {
        return semesterRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                messageSource.getMessage("semester.notFound", null, Locale.getDefault()))
        );
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "academic::semesters", allEntries = true),
            @CacheEvict(value = "academic::curriculums", allEntries = true)
    })
    public void deleteSemesterById(UUID id) {
        semesterRepo.deleteById(id);
    }

    @Override
    public boolean existDuplicateSemester(SemesterType name, UUID semesterId, UUID yearId) {
        return semesterRepo.existsByNameAndYearIdAndIdNot(name, yearId, semesterId);
    }
}
