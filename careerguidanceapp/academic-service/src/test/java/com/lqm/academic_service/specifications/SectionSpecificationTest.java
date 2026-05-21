package com.lqm.academic_service.specifications;

import com.lqm.academic_service.models.Section;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionSpecificationTest {

    @Mock
    private Root<Section> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Predicate conjunctionPredicate;

    @Mock
    private Predicate equalPredicate;

    @Mock
    private Predicate andPredicate;

    @Mock
    private Path<Object> teacherIdPath;

    @Mock
    private Path<Object> classroomPath;

    @Mock
    private Path<Object> classroomIdPath;

    @Mock
    private Path<Object> curriculumPath;

    @Mock
    private Path<Object> curriculumIdPath;

    @Mock
    private Path<Object> idPath;

    @BeforeEach
    void setUp() {
        lenient().when(cb.conjunction()).thenReturn(conjunctionPredicate);
    }

    @Test
    void filterByParams_NullOrEmptyParams() {
        Specification<Section> spec = SectionSpecification.filterByParams(null);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(cb).conjunction();
        verifyNoMoreInteractions(cb, root);
    }

    @Test
    void filterByParams_WithTeacherId() {
        UUID teacherId = UUID.randomUUID();
        Map<String, String> params = Map.of("teacherId", teacherId.toString());

        when(root.get("teacherId")).thenReturn(teacherIdPath);
        when(cb.equal(teacherIdPath, teacherId)).thenReturn(equalPredicate);
        when(cb.and(conjunctionPredicate, equalPredicate)).thenReturn(andPredicate);

        Specification<Section> spec = SectionSpecification.filterByParams(params);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(root).get("teacherId");
        verify(cb).equal(teacherIdPath, teacherId);
        verify(cb).and(conjunctionPredicate, equalPredicate);
    }

    @Test
    void filterByParams_WithClassroomId() {
        UUID classroomId = UUID.randomUUID();
        Map<String, String> params = Map.of("classroomId", classroomId.toString());

        when(root.get("classroom")).thenReturn(classroomPath);
        when(classroomPath.get("id")).thenReturn(classroomIdPath);
        when(cb.equal(classroomIdPath, classroomId)).thenReturn(equalPredicate);
        when(cb.and(conjunctionPredicate, equalPredicate)).thenReturn(andPredicate);

        Specification<Section> spec = SectionSpecification.filterByParams(params);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(root).get("classroom");
        verify(classroomPath).get("id");
        verify(cb).equal(classroomIdPath, classroomId);
    }

    @Test
    void filterByParams_WithCurriculumId() {
        UUID curriculumId = UUID.randomUUID();
        Map<String, String> params = Map.of("curriculumId", curriculumId.toString());

        when(root.get("curriculum")).thenReturn(curriculumPath);
        when(curriculumPath.get("id")).thenReturn(curriculumIdPath);
        when(cb.equal(curriculumIdPath, curriculumId)).thenReturn(equalPredicate);
        when(cb.and(conjunctionPredicate, equalPredicate)).thenReturn(andPredicate);

        Specification<Section> spec = SectionSpecification.filterByParams(params);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(root).get("curriculum");
        verify(curriculumPath).get("id");
        verify(cb).equal(curriculumIdPath, curriculumId);
    }

    @Test
    void filterByParams_InvalidUUID() {
        Map<String, String> params = Map.of("teacherId", "invalid-uuid");

        Specification<Section> spec = SectionSpecification.filterByParams(params);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertNotNull(predicate);
        // It should ignore the invalid UUID and return the base conjunction
        verify(cb).conjunction();
        verifyNoMoreInteractions(cb);
    }

    @Test
    void hasIdIn_NullOrEmptyList() {
        Predicate disjunctionPredicate = mock(Predicate.class);
        when(cb.disjunction()).thenReturn(disjunctionPredicate);

        Specification<Section> spec = SectionSpecification.hasIdIn(null);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(cb).disjunction();

        spec = SectionSpecification.hasIdIn(Collections.emptyList());
        predicate = spec.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(cb, times(2)).disjunction();
    }

    @Test
    void hasIdIn_WithIds() {
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        CriteriaBuilder.In<?> inClause = mock(CriteriaBuilder.In.class);
        when(root.get("id")).thenReturn(idPath);
        when(idPath.in(ids)).thenReturn(inClause);

        Specification<Section> spec = SectionSpecification.hasIdIn(ids);
        Predicate predicate = spec.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(root).get("id");
        verify(idPath).in(ids);
    }
}
