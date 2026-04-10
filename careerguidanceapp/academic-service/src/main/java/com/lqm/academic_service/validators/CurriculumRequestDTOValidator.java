package com.lqm.academic_service.validators;

import com.lqm.academic_service.dtos.CurriculumRequestDTO;
import com.lqm.academic_service.services.CurriculumService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class CurriculumRequestDTOValidator implements Validator, SupportsClass {
    
    private final CurriculumService curriculumService;
    
    @Override
    public Class<?> getSupportedClass() {
        return CurriculumRequestDTO.class;
    }

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return CurriculumRequestDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        CurriculumRequestDTO curriculumRequestDTO = (CurriculumRequestDTO) target;
        
        boolean exists = curriculumService.existDuplicateCurriculum(
                curriculumRequestDTO.gradeId(),
                curriculumRequestDTO.semesterId(),
                curriculumRequestDTO.subjectId(),
                curriculumRequestDTO.id()
        );
        if (exists) {
            errors.rejectValue("semesterId", "curriculum.unique");
            errors.rejectValue("subjectId", "curriculum.unique");
        }
    }
}
