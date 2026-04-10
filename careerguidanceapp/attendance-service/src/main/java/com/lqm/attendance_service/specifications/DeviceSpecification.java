package com.lqm.attendance_service.specifications;

import com.lqm.attendance_service.models.Device;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.*;

public class DeviceSpecification {
    public static Specification<Device> filterByParams(Map<String, String> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params == null || params.isEmpty()) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            String kw = params.get("kw");
            if (StringUtils.hasText(kw)) {
                String likeKw = "%" + kw.trim().toLowerCase() + "%";

                predicates.add(cb.like(root.get("id"), likeKw));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
