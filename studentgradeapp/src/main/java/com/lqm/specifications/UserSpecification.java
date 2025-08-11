package com.lqm.specifications;

import com.lqm.models.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty())
                return cb.conjunction(); // no filter
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), kw),
                    cb.like(cb.lower(root.get("lastName")), kw)
            );
        };
    }

    public static Specification<User> hasRole(String role) {
        return (root, query, cb) -> {
            if (role == null || role.isEmpty())
                return cb.conjunction();
            return cb.equal(root.get("role"), role);
        };
    }
}
