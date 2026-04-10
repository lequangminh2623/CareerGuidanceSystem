package com.lqm.academic_service.clients;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "api-gateway", path = "/attendance-service/api/internal/admin/attendances", contextId = "attendanceClient")
public interface AttendanceClient {
    @DeleteMapping("/classrooms/{classroomId}")
    void deleteAttendancesForClassroom(@PathVariable UUID classroomId, @RequestBody List<UUID> studentIds);
}
