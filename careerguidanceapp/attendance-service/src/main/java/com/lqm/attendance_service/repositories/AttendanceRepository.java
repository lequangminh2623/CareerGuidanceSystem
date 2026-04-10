package com.lqm.attendance_service.repositories;

import com.lqm.attendance_service.models.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByClassroomIdAndAttendanceDate(UUID classroomId, LocalDate attendanceDate);

    void deleteByClassroomIdAndAttendanceDate(UUID classroomId, LocalDate attendanceDate);

    Optional<Attendance> findByStudentIdAndAttendanceDate(UUID studentId, LocalDate attendanceDate);

    List<Attendance> findByStudentIdAndClassroomId(UUID studentId, UUID classroomId);

    void deleteByClassroomIdAndStudentIdIn(UUID classroomId, List<UUID> studentIds);

    List<Attendance> findByStudentId(UUID studentId);

    boolean existsAttendanceByStudentIdAndAttendanceDate(UUID studentId, LocalDate attendanceDate);

    @Query("SELECT a.studentId FROM Attendance a WHERE a.attendanceDate = :today")
    Set<UUID> findPresentStudentIdsByDate(@Param("today") LocalDate today);
}
