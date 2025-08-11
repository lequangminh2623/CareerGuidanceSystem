package com.lqm.specifications;

import com.lqm.models.Classroom;
import com.lqm.models.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

public class ClassroomSpecification {

    // Dành cho các bộ lọc cơ bản theo params (search, semester, course, lecturer)
    public static Specification<Classroom> filterByParams(Map<String, String> params) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

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

    // Mở rộng thêm điều kiện theo User (lecturer hoặc student)
    public static Specification<Classroom> filterByParamsAndUser(User user, Map<String, String> params) {
        return (root, query, cb) -> {
            var predicate = filterByParams(params).toPredicate(root, query, cb);

            if (user != null && user.getRole() != null) {
                switch (user.getRole()) {
                    case "ROLE_LECTURER":
                        predicate = cb.and(predicate,
                                cb.equal(root.get("lecturer").get("id"), user.getId()));
                        break;
                    case "ROLE_STUDENT":
                        Join<Object, Object> studentJoin = root.join("studentSet", JoinType.INNER);
                        predicate = cb.and(predicate,
                                cb.equal(studentJoin.get("id"), user.getId()));
                        break;
                }
            }

            return predicate;
        };
    }
}
