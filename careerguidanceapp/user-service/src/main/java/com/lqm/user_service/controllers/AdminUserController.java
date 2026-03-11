package com.lqm.user_service.controllers;

import com.lqm.user_service.dtos.AdminUserRequestDTO;
import com.lqm.user_service.dtos.UserDetailsResponseDTO;
import com.lqm.user_service.dtos.UserResponseDTO;
import com.lqm.user_service.mappers.UserMapper;
import com.lqm.user_service.models.User;
import com.lqm.user_service.services.UserService;
import com.lqm.user_service.utils.PageSize;
import com.lqm.user_service.utils.PageableUtil;
import com.lqm.user_service.validators.WebAppValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/admin/users")
@CrossOrigin
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final PageableUtil pageableUtil;
    private final UserMapper userMapper;
    private final WebAppValidator webAppValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @PostMapping
    Page<UserResponseDTO> getUsers(@RequestBody List<UUID> ids, @RequestParam Map<String, String> params) {
        Pageable pageable = pageableUtil.getPageable(
                params.getOrDefault("page", "1"),
                PageSize.USER_PAGE_SIZE,
                List.of("lastName:acs")
        );
        Page<User> users = userService.getUsers(ids, params, pageable);

        return users.map(userMapper::toUserResponseDTO);
    }

    @GetMapping("/details")
    public Page<UserDetailsResponseDTO> getUsersDetails(@RequestParam Map<String, String> params) {
        Pageable pageable = pageableUtil.getPageable(
                params.getOrDefault("page", "1"),
                PageSize.USER_PAGE_SIZE,
                List.of("lastName:acs")
        );
        Page<User> users = userService.getUsers(List.of(), params, pageable);

        return users.map(userMapper::toUserDetailsResponseDTO);
    }

    @GetMapping("/{id}/request")
    public AdminUserRequestDTO getUserRequestById(@PathVariable UUID id) {
        return userMapper.toAdminUserRequestDTO(userService.getUserById(id));
    }

    @GetMapping("/{id}/response")
    public UserResponseDTO getUserResponseById(@PathVariable UUID id) {
        return userMapper.toUserResponseDTO(userService.getUserById(id));
    }

    @PostMapping(path = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void saveUser(@RequestPart("user") @Valid AdminUserRequestDTO adminUserRequestDTO,
                         @RequestPart("file") MultipartFile file) {
        User user = userMapper.toEntity(adminUserRequestDTO);
        userService.saveUser(user, file, adminUserRequestDTO.code());
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable UUID id) {
        userService.deleteUser(id);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(userService.getUserStatistics());
    }

}
