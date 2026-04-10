package com.lqm.attendance_service.services;

public interface RedisAttendanceWorker {
    void processAbsentQueue();
}
