package com.lqm.academic_service.clients;

import com.lqm.academic_service.dtos.SyncScoreRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/score-service/api/internal/admin/scores", contextId = "scoreClient")
public interface ScoreClient {

    @DeleteMapping("/students/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteScoreDetail(@PathVariable("studentId") UUID studentId, @RequestBody List<UUID> sectionIds);

    @PostMapping("/sync")
    void syncScoresForClassroom(@RequestBody SyncScoreRequestDTO request);

}