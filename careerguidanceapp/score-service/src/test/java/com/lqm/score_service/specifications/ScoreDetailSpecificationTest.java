package com.lqm.score_service.specifications;

import com.lqm.score_service.models.ScoreDetail;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScoreDetailSpecification Unit Tests")
class ScoreDetailSpecificationTest {

    @Mock private Root<ScoreDetail> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder cb;

    @Mock private Predicate conjunctionPredicate;
    @Mock private Predicate equalPredicate;
    @Mock private Predicate andPredicate;

    @Mock private Path<Object> sectionIdPath;
    @Mock private Path<Object> studentIdPath;
    @Mock private Path<Object> sectionPath;
    @Mock private Path<Object> subjectIdPath;
    @Mock private Path<Object> semesterIdPath;
    @Mock private Path<Object> classroomIdPath;
    @Mock private Path<Object> teacherIdPath;

    @BeforeEach
    void setUp() {
        lenient().when(cb.conjunction()).thenReturn(conjunctionPredicate);
    }

    @Test
    @DisplayName("filter: Returns base conjunction when parameters are null")
    void filter_NullParams_ReturnsConjunction() {
        Specification<ScoreDetail> spec = ScoreDetailSpecification.filter(null);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(cb).conjunction();
        verifyNoMoreInteractions(cb, root);
    }

    @Test
    @DisplayName("filter: Returns base conjunction when parameters are empty")
    void filter_EmptyParams_ReturnsConjunction() {
        Specification<ScoreDetail> spec = ScoreDetailSpecification.filter(Map.of());
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(cb).conjunction();
        verifyNoMoreInteractions(cb, root);
    }

    @Test
    @DisplayName("filter: Adds sectionId and studentId constraints correctly")
    void filter_WithSectionIdAndStudentId_AddsPredicates() {
        UUID sectionId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        Map<String, String> params = Map.of(
                "sectionId", sectionId.toString(),
                "studentId", studentId.toString()
        );

        when(root.get("sectionId")).thenReturn(sectionIdPath);
        when(root.get("studentId")).thenReturn(studentIdPath);

        when(cb.equal(sectionIdPath, sectionId)).thenReturn(equalPredicate);
        when(cb.equal(studentIdPath, studentId)).thenReturn(equalPredicate);

        when(cb.and(any(Predicate.class), eq(equalPredicate))).thenReturn(andPredicate);

        Specification<ScoreDetail> spec = ScoreDetailSpecification.filter(params);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(root).get("sectionId");
        verify(root).get("studentId");
        verify(cb, times(2)).equal(any(), any(UUID.class));
    }

    @Test
    @DisplayName("filter: Adds nested Section constraints correctly (subjectId, semesterId, classroomId, teacherId)")
    void filter_WithNestedSectionConstraints_AddsPredicates() {
        UUID subjectId = UUID.randomUUID();
        UUID semesterId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        Map<String, String> params = Map.of(
                "subjectId", subjectId.toString(),
                "semesterId", semesterId.toString(),
                "classroomId", classroomId.toString(),
                "teacherId", teacherId.toString()
        );

        when(root.get("section")).thenReturn(sectionPath);
        
        when(sectionPath.get("subjectId")).thenReturn(subjectIdPath);
        when(sectionPath.get("semesterId")).thenReturn(semesterIdPath);
        when(sectionPath.get("classroomId")).thenReturn(classroomIdPath);
        when(sectionPath.get("teacherId")).thenReturn(teacherIdPath);

        when(cb.equal(subjectIdPath, subjectId)).thenReturn(equalPredicate);
        when(cb.equal(semesterIdPath, semesterId)).thenReturn(equalPredicate);
        when(cb.equal(classroomIdPath, classroomId)).thenReturn(equalPredicate);
        when(cb.equal(teacherIdPath, teacherId)).thenReturn(equalPredicate);

        when(cb.and(any(Predicate.class), eq(equalPredicate))).thenReturn(andPredicate);

        Specification<ScoreDetail> spec = ScoreDetailSpecification.filter(params);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(root, times(4)).get("section");
        verify(sectionPath).get("subjectId");
        verify(sectionPath).get("semesterId");
        verify(sectionPath).get("classroomId");
        verify(sectionPath).get("teacherId");
        verify(cb, times(4)).equal(any(), any(UUID.class));
    }

    @Test
    @DisplayName("filter: Ignores invalid UUID formats gracefully")
    void filter_InvalidUUIDs_IgnoresAndReturnsBaseConjunction() {
        Map<String, String> params = Map.of(
                "sectionId", "invalid-uuid",
                "studentId", "not-a-uuid"
        );

        Specification<ScoreDetail> spec = ScoreDetailSpecification.filter(params);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(cb).conjunction();
        // Since UUID parsing fails, it should never call root.get()
        verifyNoInteractions(root);
    }
}
