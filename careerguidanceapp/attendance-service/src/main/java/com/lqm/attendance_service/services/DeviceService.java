package com.lqm.attendance_service.services;

import com.lqm.attendance_service.models.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DeviceService {

    Device getDeviceById(String deviceId);

    Page<Device> getAllDevices(Map<String, String> params, Pageable pageable);

    List<Device> getDevicesWithoutClassroom();

    Device getDeviceByClassroom(UUID classroomId);

    void updateDeviceActiveStatus(String id, boolean active, boolean notifyMqtt);

    Device saveDevice(Device device);

    Device assignDeviceToClassroom(Device device);

    void unassignDeviceFromClassroom(String deviceId);

    void deleteDevice(String deviceId);

    void unassignDeviceByClassroomId(UUID classroomId);

    Map<UUID, String> buildClassroomMap(List<Device> devices);

    boolean existDeviceById(String id);

    boolean existDeviceByClassroomId(UUID classroomId);
}
