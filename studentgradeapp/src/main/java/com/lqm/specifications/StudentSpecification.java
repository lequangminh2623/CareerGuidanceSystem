package com.lqm.specifications;

import com.lqm.models.Student;
import com.lqm.models.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class StudentSpecification {

    public static Specification<Student> hasKeyword(String kw) {
        if (kw == null || kw.trim().isEmpty()) {
            return null;
        }
        String pattern = "%" + kw.toLowerCase() + "%";
        return (root, query, cb) -> {
            Join<Student, User> userJoin = root.join("user");
            Predicate firstNameLike = cb.like(cb.lower(userJoin.get("firstName")), pattern);
            Predicate lastNameLike  = cb.like(cb.lower(userJoin.get("lastName")), pattern);
            Predicate codeLike      = cb.like(cb.lower(root.get("code")), pattern);
            return cb.or(firstNameLike, lastNameLike, codeLike);
        };
    }

    public static Specification<Student> hasRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("user").get("role"), role);
    }

    public static Specification<Student> buildSpecification(String kw, String role) {
        Specification<Student> spec = hasKeyword(kw);

        if (hasRole(role) != null) {
            spec = spec == null ? hasRole(role) : spec.and(hasRole(role));
        }

        return spec;
    }

}
