package com.lqm.score_service.clients;

import com.lqm.score_service.dtos.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/user-service/api/internal/secure/users", contextId = "userClient")
public interface UserClient {

    @GetMapping("/{id}/exist")
    Boolean checkStudentExistById(@PathVariable("id") UUID id);

    @GetMapping("/me")
    UserResponseDTO getCurrentUser();
}
