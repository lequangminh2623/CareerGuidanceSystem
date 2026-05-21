package com.lqm.academic_service.validators;

import com.lqm.academic_service.dtos.SectionListRequest;
import com.lqm.academic_service.dtos.SectionRequestDTO;
import com.lqm.academic_service.services.SectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionListRequestValidatorTest {

    @Mock
    private SectionService sectionService;

    @InjectMocks
    private SectionListRequestValidator validator;

    private UUID classroomId;
    private UUID curriculumId1;
    private UUID curriculumId2;

    @BeforeEach
    void setUp() {
        classroomId = UUID.randomUUID();
        curriculumId1 = UUID.randomUUID();
        curriculumId2 = UUID.randomUUID();
    }

    @Test
    void supports() {
        assertTrue(validator.supports(SectionListRequest.class));
        assertFalse(validator.supports(Object.class));
    }

    @Test
    void validate_EmptySections() {
        SectionListRequest request = new SectionListRequest(List.of());
        Errors errors = new BeanPropertyBindingResult(request, "request");

        validator.validate(request, errors);

        assertFalse(errors.hasErrors());
        verifyNoInteractions(sectionService);
    }

    @Test
    void validate_NullSharedClassroomId() {
        SectionRequestDTO section = new SectionRequestDTO(null, null, null, curriculumId1, null);
        SectionListRequest request = new SectionListRequest(List.of(section));
        Errors errors = new BeanPropertyBindingResult(request, "request");

        validator.validate(request, errors);

        assertFalse(errors.hasErrors());
        verifyNoInteractions(sectionService);
    }

    @Test
    void validate_DuplicateCurriculumInRequest() {
        SectionRequestDTO section1 = new SectionRequestDTO(null, null, classroomId, curriculumId1, null);
        SectionRequestDTO section2 = new SectionRequestDTO(null, null, classroomId, curriculumId1, null);
        SectionListRequest request = new SectionListRequest(List.of(section1, section2));
        Errors errors = new BeanPropertyBindingResult(request, "request");

        when(sectionService.getExistingCurriculumMap(eq(classroomId), anySet())).thenReturn(Map.of());

        validator.validate(request, errors);

        assertTrue(errors.hasFieldErrors("sections[1].curriculumId"));
        assertEquals("duplicate.in.request", errors.getFieldError("sections[1].curriculumId").getCode());
    }

    @Test
    void validate_DuplicateCurriculumInDatabase() {
        UUID existingSectionId = UUID.randomUUID();
        SectionRequestDTO section1 = new SectionRequestDTO(null, null, classroomId, curriculumId1, null);
        SectionListRequest request = new SectionListRequest(List.of(section1));
        Errors errors = new BeanPropertyBindingResult(request, "request");

        when(sectionService.getExistingCurriculumMap(classroomId, Set.of(curriculumId1)))
                .thenReturn(Map.of(curriculumId1, existingSectionId));

        validator.validate(request, errors);

        assertTrue(errors.hasFieldErrors("sections[0].curriculumId"));
        assertEquals("duplicate.in.db", errors.getFieldError("sections[0].curriculumId").getCode());
    }

    @Test
    void validate_UpdateExistingSection_Success() {
        UUID existingSectionId = UUID.randomUUID();
        // ID of the section in request matches the one in DB, meaning it's an update
        SectionRequestDTO section1 = new SectionRequestDTO(existingSectionId, null, classroomId, curriculumId1, null);
        SectionListRequest request = new SectionListRequest(List.of(section1));
        Errors errors = new BeanPropertyBindingResult(request, "request");

        when(sectionService.getExistingCurriculumMap(classroomId, Set.of(curriculumId1)))
                .thenReturn(Map.of(curriculumId1, existingSectionId));

        validator.validate(request, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    void validate_Success() {
        SectionRequestDTO section1 = new SectionRequestDTO(null, null, classroomId, curriculumId1, null);
        SectionRequestDTO section2 = new SectionRequestDTO(null, null, classroomId, curriculumId2, null);
        SectionListRequest request = new SectionListRequest(List.of(section1, section2));
        Errors errors = new BeanPropertyBindingResult(request, "request");

        when(sectionService.getExistingCurriculumMap(classroomId, Set.of(curriculumId1, curriculumId2)))
                .thenReturn(Map.of());

        validator.validate(request, errors);

        assertFalse(errors.hasErrors());
    }
}
