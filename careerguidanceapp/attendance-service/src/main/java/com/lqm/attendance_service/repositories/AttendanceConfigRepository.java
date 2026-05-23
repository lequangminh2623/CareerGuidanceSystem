package com.lqm.attendance_service.repositories;

import com.lqm.attendance_service.models.AttendanceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceConfigRepository extends JpaRepository<AttendanceConfig, Long> {
}
