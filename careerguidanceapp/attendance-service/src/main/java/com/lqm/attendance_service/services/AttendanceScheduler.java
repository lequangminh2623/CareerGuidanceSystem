package com.lqm.attendance_service.services;

public interface AttendanceScheduler {
    void activateDevicesAt6AM();
    void processEndOfDayAt5PM();
}
