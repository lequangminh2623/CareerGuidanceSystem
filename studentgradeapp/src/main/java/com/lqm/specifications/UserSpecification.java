package com.lqm.specifications;

import com.lqm.models.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserSpecification {

    public static Specification<User> filterByParams(Map<String, String> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params != null) {
                // Tìm kiếm theo keyword (firstName hoặc lastName)
                String kw = params.get("kw");
                if (kw != null && !kw.trim().isEmpty()) {
                    String likeKw = "%" + kw.trim().toLowerCase() + "%";
                    predicates.add(
                            cb.like(cb.lower(cb.concat(cb.concat(root.get("lastName"), " "), root.get("firstName"))), likeKw));
                }

                // Lọc theo role
                String role = params.get("role");
                if (role != null && !role.trim().isEmpty()) {
                    predicates.add(cb.equal(root.get("role"), role));
                }
            }

            // Sắp xếp
            query.orderBy(cb.desc(root.get("id")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
