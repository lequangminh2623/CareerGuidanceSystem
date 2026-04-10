package com.lqm.academic_service.clients;

import com.lqm.academic_service.dtos.SyncScoreRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "api-gateway", path = "/score-service/api/internal/admin/scores", contextId = "scoreAdminClient")
public interface ScoreAdminClient {

    @PostMapping("/sync")
    void syncScoresForClassroom(@RequestBody SyncScoreRequestDTO request);

}