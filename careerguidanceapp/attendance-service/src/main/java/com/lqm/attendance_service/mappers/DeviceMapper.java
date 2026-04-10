package com.lqm.attendance_service.mappers;

import com.lqm.attendance_service.dtos.DeviceRequestDTO;
import com.lqm.attendance_service.dtos.DeviceResponseDTO;
import com.lqm.attendance_service.dtos.DeviceStatusDTO;
import com.lqm.attendance_service.models.Device;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface DeviceMapper {
    @Mapping(target = "classroomName", source = "classroomId")
    DeviceResponseDTO toDeviceResponseDTO(Device device, @Context Map<UUID, String> classroomMap);

    default String mapClassroomName(UUID classroomId, @Context Map<UUID, String> classroomMap) {
        if (classroomId == null || classroomMap == null) {
            return null;
        }
        return classroomMap.get(classroomId);
    }

    DeviceStatusDTO toDeviceStatusDTO(Device entity);

    Device toEntity(DeviceRequestDTO dto);

    Device toEntity(DeviceStatusDTO dto);
}
