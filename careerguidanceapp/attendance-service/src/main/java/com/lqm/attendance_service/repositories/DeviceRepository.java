package com.lqm.attendance_service.repositories;

import com.lqm.attendance_service.models.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String>, JpaSpecificationExecutor<Device> {
    Optional<Device> findByClassroomId(UUID classroomId);

    boolean existsByClassroomId(UUID classroomId);
}
