package com.lqm.attendance_service.services;

import java.util.UUID;

import com.lqm.attendance_service.models.Fingerprint;

public interface FingerprintService {

    Fingerprint getFingerprintByFingerprintIndexAndClassroomId(Integer fingerprintIndex, UUID classroomId);

    Fingerprint saveFingerprint(Fingerprint fingerprint);

    void deleteFingerprintsByClassroomAndStudentIds(UUID classroomId, java.util.List<UUID> studentIds);

    void deleteFingerprintsByClassroomId(UUID classroomId);
}
