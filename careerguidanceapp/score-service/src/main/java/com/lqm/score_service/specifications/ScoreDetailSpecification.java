package com.lqm.score_service.specifications;

import com.lqm.score_service.models.ScoreDetail;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.Map;
import java.util.UUID;

public class ScoreDetailSpecification {

    public static Specification<ScoreDetail> filter(Map<String, String> params) {
        return (root, query, cb) -> {

            Predicate predicate = cb.conjunction();

            if (params == null || params.isEmpty()) {
                return predicate;
            }

            String sectionIdStr = params.get("sectionId");
            if (sectionIdStr != null && !sectionIdStr.isBlank()) {
                try {
                    UUID sectionId = UUID.fromString(sectionIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("sectionId"), sectionId));
                } catch (IllegalArgumentException ignored) {
                }
            }

            String studentIdStr = params.get("studentId");
            if (studentIdStr != null && !studentIdStr.isBlank()) {
                try {
                    UUID studentId = UUID.fromString(studentIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("studentId"), studentId));
                } catch (IllegalArgumentException ignored) {
                }
            }

            String subjectIdStr = params.get("subjectId");
            if (subjectIdStr != null && !subjectIdStr.isBlank()) {
                try {
                    UUID subjectId = UUID.fromString(subjectIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("section").get("subjectId"), subjectId));
                } catch (IllegalArgumentException ignored) {
                }
            }

            String semesterIdStr = params.get("semesterId");
            if (semesterIdStr != null && !semesterIdStr.isBlank()) {
                try {
                    UUID semesterId = UUID.fromString(semesterIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("section").get("semesterId"), semesterId));
                } catch (IllegalArgumentException ignored) {
                }
            }

            String classroomIdStr = params.get("classroomId");
            if (classroomIdStr != null && !classroomIdStr.isBlank()) {
                try {
                    UUID classroomId = UUID.fromString(classroomIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("section").get("classroomId"), classroomId));
                } catch (IllegalArgumentException ignored) {
                }
            }

            String teacherIdStr = params.get("teacherId");
            if (teacherIdStr != null && !teacherIdStr.isBlank()) {
                try {
                    UUID teacherId = UUID.fromString(teacherIdStr);
                    predicate = cb.and(predicate,
                            cb.equal(root.get("section").get("teacherId"), teacherId));
                } catch (IllegalArgumentException ignored) {
                }
            }

            return predicate;
        };
    }
}