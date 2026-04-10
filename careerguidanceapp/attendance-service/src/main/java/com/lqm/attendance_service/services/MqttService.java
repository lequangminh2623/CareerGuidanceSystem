package com.lqm.attendance_service.services;

import com.lqm.attendance_service.dtos.FingerprintRequestDTO;

public interface MqttService {
    void togglePower(String deviceId, Boolean active);

    void startEnrollment(String deviceId, FingerprintRequestDTO request);

    void cancelEnrollment(String deviceId);

    void deleteFingerprint(String deviceId, Integer fingerprintIndex);

    void clearAllFingerprints(String deviceId);
}
