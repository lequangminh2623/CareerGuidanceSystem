package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.FingerprintRequestDTO;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "api-gateway", path = "/attendance-service/api/internal/admin/fingerprints", contextId = "fingerprintClient")
public interface FingerprintClient {

    @PostMapping("/enroll")
    void enrollFingerprint(@RequestBody FingerprintRequestDTO request);

    @PostMapping("/cancel")
    void cancelEnrollment(@RequestBody UUID classroomId);
}
