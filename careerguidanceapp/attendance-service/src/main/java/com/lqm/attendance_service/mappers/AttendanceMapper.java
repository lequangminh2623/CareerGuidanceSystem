package com.lqm.attendance_service.mappers;

import com.lqm.attendance_service.dtos.AdminAttendanceRequestDTO;
import com.lqm.attendance_service.dtos.AttendanceResponseDTO;
import com.lqm.attendance_service.models.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface AttendanceMapper {
    @Mapping(target = "status", source = "status.name")
    AdminAttendanceRequestDTO toAttendanceRequestDTO(Attendance entity);

    @Mapping(target = "status", source = "status.name")
    AttendanceResponseDTO toAttendanceResponseDTO(Attendance entity);

    @Mapping(target = "status", expression = "java(com.lqm.attendance_service.models.AttendanceStatus.fromStatusName(dto.status()))")
    Attendance toEntity(AdminAttendanceRequestDTO dto, UUID classroomId, LocalDate attendanceDate);
}
