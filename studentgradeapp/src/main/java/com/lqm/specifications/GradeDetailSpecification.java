package com.lqm.specifications;

import com.lqm.models.Classroom;
import com.lqm.models.GradeDetail;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

public class GradeDetailSpecification {

    public static Specification<GradeDetail> filter(Map<String, Integer> params, Classroom classroom) {
        return (Root<GradeDetail> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            if (params.containsKey("studentId")) {
                predicate = cb.and(predicate, cb.equal(root.get("student").get("id"), params.get("studentId")));
            }

            if (params.containsKey("courseId")) {
                predicate = cb.and(predicate, cb.equal(root.get("course").get("id"), params.get("courseId")));
            }

            if (params.containsKey("semesterId")) {
                predicate = cb.and(predicate, cb.equal(root.get("semester").get("id"), params.get("semesterId")));
            }

            if (classroom != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("course").get("id"), classroom.getCourse().getId()),
                        cb.equal(root.get("semester").get("id"), classroom.getSemester().getId())
                );
            }

            return predicate;
        };
    }
}
