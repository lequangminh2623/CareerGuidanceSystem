package com.lqm.academic_service.validators;

import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.ClassroomRequestDTO;
import com.lqm.academic_service.dtos.UserResponseDTO;
import com.lqm.academic_service.models.Grade;
import com.lqm.academic_service.models.GradeType;
import com.lqm.academic_service.models.Year;
import com.lqm.academic_service.services.ClassroomService;
import com.lqm.academic_service.services.GradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassroomRequestDTOValidatorTest {

    @Mock
    private ClassroomService classroomService;

    @Mock
    private UserClient userClient;

    @Mock
    private MessageSource messageSource;

    @Mock
    private GradeService gradeService;

    @InjectMocks
    private ClassroomRequestDTOValidator validator;

    private ClassroomRequestDTO validRequest;
    private Errors errors;
    private UUID gradeId;
    private UUID yearId;
    private UUID classId;

    @BeforeEach
    void setUp() {
        gradeId = UUID.randomUUID();
        yearId = UUID.randomUUID();
        classId = UUID.randomUUID();
        validRequest = new ClassroomRequestDTO(classId, "10A1", gradeId, null);
        errors = new BeanPropertyBindingResult(validRequest, "classroomRequestDTO");
    }

    @Test
    void supports() {
        assertTrue(validator.supports(ClassroomRequestDTO.class));
        assertFalse(validator.supports(Object.class));
    }

    @Test
    void validate_NameIsNull() {
        ClassroomRequestDTO request = new ClassroomRequestDTO(classId, null, gradeId, null);
        errors = new BeanPropertyBindingResult(request, "classroomRequestDTO");

        validator.validate(request, errors);

        assertTrue(errors.hasFieldErrors("name"));
        assertEquals("classroom.name.notNull", errors.getFieldError("name").getCode());
        verifyNoInteractions(classroomService, userClient, messageSource, gradeService);
    }

    @Test
    void validate_NameIsEmpty() {
        ClassroomRequestDTO request = new ClassroomRequestDTO(classId, "   ", gradeId, null);
        errors = new BeanPropertyBindingResult(request, "classroomRequestDTO");

        validator.validate(request, errors);

        assertTrue(errors.hasFieldErrors("name"));
        assertEquals("classroom.name.notNull", errors.getFieldError("name").getCode());
        verifyNoInteractions(classroomService, userClient, messageSource, gradeService);
    }

    @Test
    void validate_DuplicateNameInGrade() {
        when(classroomService.existDuplicateClassroom("10A1", gradeId, classId)).thenReturn(true);

        validator.validate(validRequest, errors);

        assertTrue(errors.hasFieldErrors("name"));
        assertEquals("classroom.unique", errors.getFieldError("name").getCode());
        verify(classroomService).existDuplicateClassroom("10A1", gradeId, classId);
    }

    @Test
    void validate_ConflictingStudents() {
        UUID studentId = UUID.randomUUID();
        ClassroomRequestDTO request = new ClassroomRequestDTO(classId, "10A1", gradeId, List.of(studentId));
        errors = new BeanPropertyBindingResult(request, "classroomRequestDTO");

        when(classroomService.existDuplicateClassroom(anyString(), any(UUID.class), any(UUID.class))).thenReturn(false);

        Year year = new Year();
        year.setId(yearId);
        Grade grade = new Grade();
        grade.setId(gradeId);
        grade.setName(GradeType.GRADE_10);
        grade.setYear(year);
        
        when(gradeService.getGradeById(gradeId)).thenReturn(grade);

        UserResponseDTO userResponse = new UserResponseDTO(studentId, "STU01", "John", "Doe");
        when(userClient.getUsers(List.of(studentId), Map.of("role", "Student")))
                .thenReturn(new PageImpl<>(List.of(userResponse)));

        when(classroomService.getStudentsInOtherClassrooms(List.of(studentId), yearId, classId))
                .thenReturn(List.of(studentId));

        when(messageSource.getMessage(eq("classroom.students.alreadyIn"), any(), eq(Locale.getDefault())))
                .thenReturn("Student already in another class");

        validator.validate(request, errors);

        assertTrue(errors.hasFieldErrors("studentIds"));
        String defaultMsg = errors.getFieldError("studentIds").getDefaultMessage();
        assertTrue(defaultMsg.contains("Student already in another class"));
        assertTrue(defaultMsg.contains("STU01 - Doe John"));
    }

    @Test
    void validate_Success() {
        when(classroomService.existDuplicateClassroom(anyString(), any(UUID.class), any(UUID.class))).thenReturn(false);

        validator.validate(validRequest, errors);

        assertFalse(errors.hasErrors());
        verify(classroomService).existDuplicateClassroom("10A1", gradeId, classId);
        verifyNoMoreInteractions(classroomService);
        verifyNoInteractions(userClient, messageSource, gradeService);
    }
}
