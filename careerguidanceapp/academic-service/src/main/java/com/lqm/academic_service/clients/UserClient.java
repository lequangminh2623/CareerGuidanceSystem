package com.lqm.academic_service.clients;

import com.lqm.academic_service.dtos.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/internal/secure/users", contextId = "userClient")
public interface UserClient {

    @PostMapping
    Page<UserResponseDTO> getUsers(@RequestBody List<UUID> ids, @RequestParam Map<String, String> params);
}
