package com.lqm.academic_service.specifications;

import com.lqm.academic_service.models.Section;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SectionSpecification {
    public static Specification<Section> filterByParams(Map<String, String> params) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (params == null || params.isEmpty()) {
                return predicate;
            }

            String teacherIdStr = params.get("teacherId");
            if (teacherIdStr != null && !teacherIdStr.isBlank()) {
                try {
                    UUID teacherId = UUID.fromString(teacherIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("teacherId"), teacherId));
                } catch (IllegalArgumentException ignored) {
                }
            }

            String classroomIdStr = params.get("classroomId");
            if (classroomIdStr != null && !classroomIdStr.isBlank()) {
                try {
                    UUID classroomId = UUID.fromString(classroomIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("classroom").get("id"), classroomId));
                } catch (IllegalArgumentException ignored) {
                }
            }

            String curriculumIdStr = params.get("curriculumId");
            if (curriculumIdStr != null && !curriculumIdStr.isBlank()) {
                try {
                    UUID curriculumId = UUID.fromString(curriculumIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("curriculum").get("id"), curriculumId));
                } catch (IllegalArgumentException ignored) {
                }
            }

            return predicate;
        };
    }

    public static Specification<Section> hasIdIn(List<UUID> sectionIds) {
        return (Root<Section> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (sectionIds == null || sectionIds.isEmpty()) {
                return cb.disjunction();
            }
            return root.get("id").in(sectionIds);
        };
    }
}
