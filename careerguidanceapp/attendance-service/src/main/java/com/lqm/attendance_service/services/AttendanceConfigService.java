package com.lqm.attendance_service.services;

import com.lqm.attendance_service.dtos.AttendanceConfigDTO;
import com.lqm.attendance_service.models.AttendanceConfig;

public interface AttendanceConfigService {
    AttendanceConfig getConfig();

    AttendanceConfig updateConfig(AttendanceConfigDTO dto);
}
