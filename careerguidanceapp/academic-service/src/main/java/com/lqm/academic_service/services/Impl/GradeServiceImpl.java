package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.models.Grade;
import com.lqm.academic_service.models.GradeType;
import com.lqm.academic_service.repositories.GradeRepository;
import com.lqm.academic_service.services.GradeService;
import com.lqm.academic_service.services.YearService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
@RequiredArgsConstructor
@Service
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepo;
    private final MessageSource messageSource;
    private final YearService yearService;

    @Override
    public List<Grade> getGradesByYearId(UUID id, Map<String, String> params) {
        String kw = (params != null) ? params.get("kw") : null;
        return gradeRepo.findByYearId(id, kw);
    }

    @Override
    public List<Grade> getGrades(Map<String, String> params) {
        String kw = (params != null) ? params.get("kw") : null;
        return gradeRepo.findAllByKeyword(kw);
    }

    @Override
    public Grade saveGrade(Grade grade, UUID yearId) {
        grade.setYear(yearService.getYearById(yearId));
        gradeRepo.save(grade);
        return grade;
    }

    @Override
    public Grade getGradeById(UUID id) {
        return gradeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                messageSource.getMessage("user.notFound", null, Locale.getDefault()))
        );
    }

    @Override
    public void deleteGradeById(UUID id) {
        gradeRepo.deleteById(id);
    }

    @Override
    public boolean existDuplicateGrade(GradeType name, UUID gradeId, UUID yearId) {
            return gradeRepo.existsByNameAndYearIdAndIdNot(name, yearId, gradeId);
    }
}
