package com.lqm.user_service.clients;

import com.lqm.user_service.dtos.UserDetailsResponseDTO;
import com.lqm.user_service.dtos.UserRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@FeignClient(
        name = "user-service",
        path = "/api/internal/secure/users",
        contextId = "userClient"
)

public interface UserClient {

    @GetMapping("/details")
    Page<UserDetailsResponseDTO> getUsersDetails(
            @RequestParam Map<String, String> params,
            @RequestParam Pageable pageable
    );

    @GetMapping("/{id}")
    UserRequestDTO getUserRequestById(@PathVariable UUID id);

    @PostMapping(path = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UserDetailsResponseDTO saveUser(
            @SpringQueryMap UserRequestDTO userRequestDTO,
            @RequestPart("file") MultipartFile file
    );

    @DeleteMapping("/{id}")
    void deleteUserById(@PathVariable UUID id);
}