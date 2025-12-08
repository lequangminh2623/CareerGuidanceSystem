package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.models.Semester;
import com.lqm.academic_service.models.SemesterType;
import com.lqm.academic_service.repositories.SemesterRepository;
import com.lqm.academic_service.services.SemesterService;
import com.lqm.academic_service.services.YearService;
import lombok.RequiredArgsConstructor;
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
    private final YearService yearService;

    @Override
    public List<Semester> getSemestersByYearId(UUID id, Map<String, String> params) {
        String kw = (params != null) ? params.get("kw") : null;
        return semesterRepo.findByYearId(id, kw);
    }

    @Override
    public Semester saveSemester(Semester semester, UUID yearId) {
        semester.setYear(yearService.getYearById(yearId));
        semesterRepo.save(semester);
        return semester;
    }

    @Override
    public Semester getSemesterById(UUID id) {
        return semesterRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                messageSource.getMessage("user.notFound", null, Locale.getDefault()))
        );
    }

    @Override
    public void deleteSemesterById(UUID id) {
        semesterRepo.deleteById(id);
    }

    @Override
    public boolean existDuplicateSemester(SemesterType name, UUID semesterId, UUID yearId) {
        return semesterRepo.existsByNameAndYearIdAndIdNot(name, yearId, semesterId);
    }
}
