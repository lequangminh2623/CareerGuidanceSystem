package com.lqm.academic_service.repositories;

import com.lqm.academic_service.models.StudentClassroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface StudentClassroomRepository extends JpaRepository<StudentClassroom, UUID> {

    Page<StudentClassroom> findByStudentId(UUID studentId, Pageable pageable);

    List<StudentClassroom> findByClassroomId(UUID classroomId);

    @Query("SELECT sc.studentId FROM StudentClassroom sc " +
            "WHERE sc.classroom.id = :classroomId AND sc.studentId IN :studentIds")
    List<UUID> findExistingIds(@Param("classroomId") UUID classroomId, @Param("studentIds") List<UUID> studentIds);

    @Query("SELECT sc.studentId FROM StudentClassroom sc " +
            "WHERE sc.studentId IN :studentIds " +
            "AND sc.classroom.grade.year.id = :yearId " +
            "AND sc.classroom.id <> :excludeClassroomId")
    List<UUID> findStudentIdsInOtherClassrooms(
            @Param("studentIds") Collection<UUID> studentIds,
            @Param("yearId") UUID yearId,
            @Param("excludeClassroomId") UUID excludeClassroomId
    );
    void deleteByClassroomIdAndStudentId(UUID classroomId, UUID studentId);

    boolean existsByClassroomId(UUID classroomId);

    boolean existsByStudentIdAndClassroomId(UUID studentId, UUID classroomId);

}
