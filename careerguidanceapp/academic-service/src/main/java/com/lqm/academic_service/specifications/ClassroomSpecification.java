package com.lqm.academic_service.specifications;

import com.lqm.academic_service.models.Classroom;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;
import java.util.UUID;

public class ClassroomSpecification {

    public static Specification<Classroom> filterByParams(Map<String, String> params) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (params == null || params.isEmpty()) {
                return predicate;
            }

            String kw = params.get("kw");
            if (kw != null && !kw.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("name")), "%" + kw.toLowerCase() + "%"));
            }

            String gradeIdStr = params.get("gradeId");
            if (gradeIdStr != null && !gradeIdStr.isBlank()) {
                try {
                    UUID gradeId = UUID.fromString(gradeIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("grade").get("id"), gradeId));
                } catch (IllegalArgumentException ignored) {}
            }

            return predicate;
        };
    }

}