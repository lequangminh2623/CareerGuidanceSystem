package com.lqm.user_service.specifications;

import com.lqm.user_service.models.Role;
import com.lqm.user_service.models.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.*;

public class UserSpecification {
    public static Specification<User> filterByParams(Map<String, String> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params == null || params.isEmpty()) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            String kw = params.get("kw");
            if (StringUtils.hasText(kw)) {
                String likeKw = "%" + kw.trim().toLowerCase() + "%";

                predicates.add(
                        cb.or(cb.like(cb.lower(cb.concat(
                                        cb.concat(root.get("lastName"), " "),
                                        root.get("firstName"))
                                ), likeKw),
                                cb.like(cb.lower(root.get("student").get("code")), likeKw)
                        )
                );
            }

            String rolesParam = params.get("role");
            if (rolesParam != null && !rolesParam.trim().isEmpty()) {
                List<String> roles = Arrays.stream(rolesParam.split(","))
                        .map(r -> Role.fromRoleName(r.trim()))
                        .filter(Objects::nonNull)
                        .map(Enum::name)
                        .toList();

                if (!roles.isEmpty()) {
                    predicates.add(root.get("role").in(roles));
                }
            }

            String activeParam = params.get("active");
            if (activeParam != null && !activeParam.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("active"), Boolean.parseBoolean(activeParam)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> hasIdIn(List<UUID> userIds) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (userIds == null || userIds.isEmpty()) {
                return cb.disjunction();
            }
            return root.get("id").in(userIds);
        };
    }
}