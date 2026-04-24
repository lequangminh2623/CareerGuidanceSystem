package com.lqm.academic_service.clients;

import com.lqm.academic_service.dtos.UserMessageResponseDTO;
import com.lqm.academic_service.dtos.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/user-service/api/internal/secure/users", contextId = "userClient")
public interface UserClient {

    @PostMapping("/batch")
    Page<UserResponseDTO> getUsers(@RequestBody List<UUID> ids, @RequestParam Map<String, String> params);

    @PostMapping("/messages")
    Page<UserMessageResponseDTO> getUsersMessages(@RequestBody List<UUID> ids,
            @RequestParam Map<String, String> params);

    @GetMapping("/me")
    UserResponseDTO getCurrentUser();

}
