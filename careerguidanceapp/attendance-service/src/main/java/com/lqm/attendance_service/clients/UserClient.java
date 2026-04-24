package com.lqm.attendance_service.clients;

import com.lqm.attendance_service.dtos.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/user-service/api/internal/secure/users", contextId = "userClient")
public interface UserClient {
    @GetMapping("/{id}/exist")
    Boolean checkStudentExistById(@PathVariable("id") UUID id);

    @GetMapping("/{id}")
    UserResponseDTO getUserById(@PathVariable("id") UUID id);

    @GetMapping("/me")
    UserResponseDTO getCurrentUser();
}
