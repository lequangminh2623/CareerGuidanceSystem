package com.lqm.attendance_service.repositories;

import com.lqm.attendance_service.models.Fingerprint;
import com.lqm.attendance_service.models.FingerprintId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FingerprintRepository extends JpaRepository<Fingerprint, FingerprintId> {
    Optional<Fingerprint> findByFingerprintIndexAndClassroomId(Integer fingerprintIndex, UUID classroomId);

    Optional<Fingerprint> findByStudentIdAndClassroomId(UUID studentId, UUID classroomId);

    @Query("SELECT DISTINCT f.studentId, f.classroomId FROM Fingerprint f")
    List<Object[]> findAllStudentAndClassroomMappings();

    void deleteByClassroomIdAndStudentIdIn(UUID classroomId, java.util.List<UUID> studentIds);

    void deleteByClassroomId(UUID classroomId);

    List<Fingerprint> findByClassroomIdAndStudentIdIn(UUID classroomId, java.util.List<UUID> studentIds);

    @Query("SELECT DISTINCT f.studentId, f.classroomId FROM Fingerprint f")
    List<Object[]> findAllFingerprintMappings();
}
