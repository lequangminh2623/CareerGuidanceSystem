package com.lqm.specifications;

import com.lqm.models.ForumPost;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ForumPostSpecification {

    public static Specification<ForumPost> filterByParams(Map<String, String> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params != null) {
                String kw = params.get("kw");
                if (kw != null && !kw.isEmpty()) {
                    predicates.add(cb.like(root.get("title"), "%" + kw + "%"));
                }

                String userId = params.get("user");
                if (userId != null && !userId.isEmpty()) {
                    predicates.add(cb.equal(root.get("user").get("id"), Integer.parseInt(userId)));
                }

                String classroomId = params.get("classroom");
                if (classroomId != null && !classroomId.isEmpty()) {
                    predicates.add(cb.equal(root.get("classroom").get("id"), Integer.parseInt(classroomId)));
                }
            }

            query.orderBy(cb.desc(root.get("id")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}