package com.lqm.academic_service.specifications;

import com.lqm.academic_service.models.Classroom;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

public class ClassroomSpecification {

    public static Specification<Classroom> filterByParams(Map<String, String> params) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

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
                    Integer gradeId = Integer.parseInt(gradeIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("grade").get("id"), gradeId));
                } catch (NumberFormatException ignored) {}
            }

            return predicate;
        };
    }

}