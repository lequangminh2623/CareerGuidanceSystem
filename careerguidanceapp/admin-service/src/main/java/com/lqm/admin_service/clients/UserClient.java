package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.UserDetailsResponseDTO;
import com.lqm.admin_service.dtos.AdminUserRequestDTO;
import com.lqm.admin_service.dtos.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/user-service/api/internal/admin/users", contextId = "userClient")
public interface UserClient {

    @PostMapping
    Page<UserResponseDTO> getUsersByIds(@RequestBody List<UUID> ids, @RequestParam Map<String, String> params);

    @GetMapping
    Page<UserResponseDTO> getUsers(@RequestParam Map<String, String> params);

    @GetMapping("/details")
    Page<UserDetailsResponseDTO> getUsersDetails(@RequestParam Map<String, String> params);

    @GetMapping("/{id}/request")
    AdminUserRequestDTO getUserRequestById(@PathVariable UUID id);

    @GetMapping("/{id}/response")
    UserResponseDTO getUserResponseById(@PathVariable UUID id);

    @PostMapping(path = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void saveUser(@RequestPart("user") AdminUserRequestDTO adminUserRequestDTO,
                  @RequestPart("file") MultipartFile file);

    @DeleteMapping("/{id}")
    void deleteUserById(@PathVariable UUID id);

    @GetMapping("/stats")
    ResponseEntity<Map<String, Object>> getStats();

}
