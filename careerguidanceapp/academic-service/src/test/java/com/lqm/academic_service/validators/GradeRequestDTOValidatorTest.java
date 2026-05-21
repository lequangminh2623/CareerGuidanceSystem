package com.lqm.academic_service.validators;

import com.lqm.academic_service.dtos.GradeRequestDTO;
import com.lqm.academic_service.models.GradeType;
import com.lqm.academic_service.services.GradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeRequestDTOValidatorTest {

    @Mock
    private GradeService gradeService;

    @InjectMocks
    private GradeRequestDTOValidator validator;

    private UUID gradeId;
    private UUID yearId;

    @BeforeEach
    void setUp() {
        gradeId = UUID.randomUUID();
        yearId = UUID.randomUUID();
    }

    @Test
    void supports() {
        assertTrue(validator.supports(GradeRequestDTO.class));
        assertFalse(validator.supports(Object.class));
    }

    @Test
    void validate_NameIsNull() {
        GradeRequestDTO request = new GradeRequestDTO(gradeId, null, yearId);
        Errors errors = new BeanPropertyBindingResult(request, "gradeRequestDTO");

        validator.validate(request, errors);

        assertTrue(errors.hasFieldErrors("name"));
        assertEquals("grade.name.notNull", errors.getFieldError("name").getCode());
        verifyNoInteractions(gradeService);
    }

    @Test
    void validate_NameIsEmpty() {
        GradeRequestDTO request = new GradeRequestDTO(gradeId, "   ", yearId);
        Errors errors = new BeanPropertyBindingResult(request, "gradeRequestDTO");

        validator.validate(request, errors);

        assertTrue(errors.hasFieldErrors("name"));
        assertEquals("grade.name.notNull", errors.getFieldError("name").getCode());
        verifyNoInteractions(gradeService);
    }

    @Test
    void validate_DuplicateGrade() {
        GradeRequestDTO request = new GradeRequestDTO(gradeId, "Grade 10", yearId);
        Errors errors = new BeanPropertyBindingResult(request, "gradeRequestDTO");

        when(gradeService.existDuplicateGrade(GradeType.GRADE_10, gradeId, yearId)).thenReturn(true);

        validator.validate(request, errors);

        assertTrue(errors.hasFieldErrors("name"));
        assertEquals("grade.unique", errors.getFieldError("name").getCode());
    }

    @Test
    void validate_Success() {
        GradeRequestDTO request = new GradeRequestDTO(gradeId, "Grade 10", yearId);
        Errors errors = new BeanPropertyBindingResult(request, "gradeRequestDTO");

        when(gradeService.existDuplicateGrade(GradeType.GRADE_10, gradeId, yearId)).thenReturn(false);

        validator.validate(request, errors);

        assertFalse(errors.hasErrors());
    }
}
