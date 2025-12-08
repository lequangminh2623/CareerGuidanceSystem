package com.lqm.academic_service.repositories;

import com.lqm.academic_service.models.StudentClassroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentClassroomRepository extends JpaRepository<StudentClassroom, UUID> {

    Page<StudentClassroom> findByStudentId(UUID studentId, Pageable pageable);

    boolean existsByStudentIdAndClassroomId(UUID studentId, UUID classroomId);

    boolean existsByStudentIdAndClassroom_Grade_IdAndClassroom_IdNot(UUID studentId, UUID gradeId, UUID excludeClassroomId);

    void deleteByClassroomIdAndStudentId(UUID classroomId, UUID studentId);

    boolean existsByClassroomId(UUID classroomId);

}
