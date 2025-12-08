package com.lqm.user_service.specifications;

import com.lqm.user_service.models.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

public class UserSpecification {
    public static Specification<User> filterByParams(Map<String, String> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params != null) {
                // 1. Lọc theo Từ khóa (kw)
                String kw = params.get("kw");
                if (kw != null && !kw.trim().isEmpty()) {
                    String likeKw = "%" + kw.trim().toLowerCase() + "%";

                    predicates.add(
                            cb.like(cb.lower(cb.concat(
                                    cb.concat(root.get("lastName"), " "),
                                    root.get("firstName"))
                            ), likeKw)
                    );
                }

                String rolesParam = params.get("role");
                if (rolesParam != null && !rolesParam.trim().isEmpty()) {
                    List<String> roles = Arrays.stream(rolesParam.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();

                    if (!roles.isEmpty()) {
                        CriteriaBuilder.In<String> inClause = cb.in(root.get("role"));
                        roles.forEach(inClause::value);
                        predicates.add(inClause);
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> hasIdIn(List<UUID> userIds) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (userIds == null || userIds.isEmpty()) {
                return cb.isTrue(cb.literal(true));
            }
            return root.get("id").in(userIds);
        };
    }
}