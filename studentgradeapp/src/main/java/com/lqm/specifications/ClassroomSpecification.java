package com.lqm.specifications;

import com.lqm.models.Classroom;
import com.lqm.models.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

public class ClassroomSpecification {

    public static Specification<Classroom> filterByParams(Map<String, String> params) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (params == null || params.isEmpty()) {
                return predicate;
            }

            String name = params.get("name");
            if (name != null && !name.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            String semesterIdStr = params.get("semesterId");
            if (semesterIdStr != null && !semesterIdStr.isBlank()) {
                try {
                    Integer semesterId = Integer.parseInt(semesterIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("semester").get("id"), semesterId));
                } catch (NumberFormatException ignored) {}
            }

            String courseIdStr = params.get("courseId");
            if (courseIdStr != null && !courseIdStr.isBlank()) {
                try {
                    Integer courseId = Integer.parseInt(courseIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("course").get("id"), courseId));
                } catch (NumberFormatException ignored) {}
            }

            String lecturerIdStr = params.get("lecturerId");
            if (lecturerIdStr != null && !lecturerIdStr.isBlank()) {
                try {
                    Integer lecturerId = Integer.parseInt(lecturerIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("lecturer").get("id"), lecturerId));
                } catch (NumberFormatException ignored) {}
            }

            return predicate;
        };
    }

    public static Specification<Classroom> filterByParamsAndUser(User user, Map<String, String> params) {
        return (root, query, cb) -> {
            var predicate = filterByParams(params).toPredicate(root, query, cb);

            if (user != null && user.getRole() != null) {
                predicate = switch (user.getRole()) {
                    case "ROLE_LECTURER" -> cb.and(predicate,
                            cb.equal(root.get("lecturer").get("id"), user.getId()));
                    case "ROLE_STUDENT" -> {
                        Join<Object, Object> studentJoin = root.join("studentSet", JoinType.INNER);
                        yield cb.and(predicate,
                                cb.equal(studentJoin.get("id"), user.getId()));
                    }
                    default -> predicate;
                };
            }

            return predicate;
        };
    }
}