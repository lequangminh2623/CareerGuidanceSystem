package com.lqm.academic_service.specifications;

import com.lqm.academic_service.models.Curriculum;
import com.lqm.academic_service.models.Section;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CurriculumSpecification {
    public static Specification<Curriculum> filterByParams(Map<String, String> params) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (params == null || params.isEmpty()) {
                return predicate;
            }

            String gradeIdStr = params.get("gradeId");
            if (gradeIdStr != null && !gradeIdStr.isBlank()) {
                try {
                    UUID gradeId = UUID.fromString(gradeIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("grade").get("id"), gradeId));
                } catch (IllegalArgumentException ignored) {}
            }

            String semesterIdStr = params.get("semesterId");
            if (semesterIdStr != null && !semesterIdStr.isBlank()) {
                try {
                    UUID semesterId = UUID.fromString(semesterIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("semester").get("id"), semesterId));
                } catch (IllegalArgumentException ignored) {}
            }

            String subjectIdStr = params.get("subjectId");
            if (subjectIdStr != null && !subjectIdStr.isBlank()) {
                try {
                    UUID subjectId = UUID.fromString(subjectIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("subject").get("id"), subjectId));
                } catch (IllegalArgumentException ignored) {}
            }

            return predicate;
        };
    }
    public static Specification<Curriculum> hasIdIn(List<UUID> curriculumIds) {
        return (Root<Curriculum> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (curriculumIds == null || curriculumIds.isEmpty()) {
                return cb.isTrue(cb.literal(true));
            }
            return root.get("id").in(curriculumIds);
        };
    }
}
