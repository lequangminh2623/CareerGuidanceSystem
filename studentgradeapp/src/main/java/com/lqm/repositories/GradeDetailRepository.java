package com.lqm.repositories;

import com.lqm.models.GradeDetail;
import com.lqm.dtos.GradeDetailDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GradeDetailRepository extends JpaRepository<GradeDetail, Integer>, JpaSpecificationExecutor<GradeDetail> {

    boolean existsByStudentIdAndCourseIdAndSemesterId(Integer studentId, Integer courseId, Integer semesterId);

    boolean existsByStudentIdAndCourseIdAndSemesterIdAndIdNot(Integer studentId, Integer courseId, Integer semesterId, Integer excludeId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM ExtraGrade e WHERE e.gradeDetail.id = :gradeDetailId AND e.gradeIndex = :gradeIndex " +
            "AND (:currentExtraGradeId IS NULL OR e.id <> :currentExtraGradeId)")
    boolean existsByGradeDetailAndGradeIndex(
            @Param("gradeDetailId") Integer gradeDetailId,
            @Param("gradeIndex") Integer gradeIndex,
            @Param("currentExtraGradeId") Integer currentExtraGradeId
    );

    @Query("SELECT new com.lqm.dtos.GradeDetailDTO(gd, c.name) " +
            "FROM GradeDetail gd " +
            "JOIN gd.student s " +
            "JOIN s.classroomSet c " +
            "WHERE s.id = :studentId " +
            "AND c.gradeStatus = 'LOCKED' " +
            "AND c.course.id = gd.course.id " +
            "AND c.semester.id = gd.semester.id " +
            "AND (:kw IS NULL OR gd.course.name LIKE %:kw%) " +
            "ORDER BY gd.semester.academicYear.id DESC, gd.semester.id DESC")
    List<GradeDetailDTO> findGradeDetailsByStudentId(
            @Param("studentId") Integer studentId,
            @Param("kw") String kw
    );

    List<GradeDetail> findAllBySemesterId(Integer semesterId);

    @Query("""
        SELECT DISTINCT gd FROM GradeDetail gd
        WHERE gd.semester.id = :semesterId AND EXISTS (
            SELECT 1 FROM Classroom c
            JOIN c.studentSet s
            WHERE c.lecturer.id = :lecturerId
            AND c.semester.id = :semesterId
            AND c.course.id = gd.course.id
            AND s.id = gd.student.id
        )
    """)
    List<GradeDetail> findByLecturerAndSemester(
            @Param("lecturerId") Integer lecturerId,
            @Param("semesterId") Integer semesterId
    );
}
