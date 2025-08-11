package com.lqm.specifications;

import com.lqm.models.Course;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import java.util.Map;

public class CourseSpecification {

    public static Specification<Course> filterByParams(Map<String, String> params) {
        return (Root<Course> root, CriteriaQuery<?> cq, CriteriaBuilder cb) -> {
            Predicate p = cb.conjunction();

            if (params != null) {
                String kw = params.get("kw");
                if (kw != null && !kw.isBlank()) {
                    p = cb.and(p, cb.like(cb.lower(root.get("name")), "%" + kw.toLowerCase() + "%"));
                }
            }

            return p;
        };
    }
}
