package com.lqm.score_service.clients;

import com.lqm.score_service.dtos.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/user-service/api/internal/secure/users", contextId = "userClient")
public interface UserClient {

    @PostMapping
    Page<UserResponseDTO> getUsers(@RequestBody List<UUID> ids, @RequestParam Map<String, String> params);

    @GetMapping("/{id}/exist")
    Boolean checkStudentExistById(@PathVariable("id") UUID id);

    @GetMapping("/current_user")
    UserResponseDTO getCurrentUser();
}
