package com.lqm.user_service.controllers;

import com.lqm.user_service.dtos.UserDetailsResponseDTO;
import com.lqm.user_service.dtos.UserMessageResponseDTO;
import com.lqm.user_service.dtos.UserResponseDTO;
import com.lqm.user_service.mappers.UserMapper;
import com.lqm.user_service.models.User;
import com.lqm.user_service.services.StudentService;
import com.lqm.user_service.services.UserService;
import com.lqm.user_service.utils.PageSize;
import com.lqm.user_service.utils.PageableUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/secure/users")
@CrossOrigin
@RequiredArgsConstructor
public class InternalUserController {

    private final StudentService studentService;
    private final UserService userService;
    private final PageableUtil pageableUtil;
    private final UserMapper userMapper;

    @PostMapping
    public Page<UserResponseDTO> getUsers(@RequestBody List<UUID> ids, @RequestParam Map<String, String> params) {
        Pageable pageable = pageableUtil.getPageable(
                params.getOrDefault("page", "1"),
                PageSize.USER_PAGE_SIZE,
                List.of());
        Page<User> userPage = userService.getUsersByIds(ids, params, pageable);

        return userPage.map(userMapper::toUserResponseDTO);
    }

    @PostMapping("/messages")
    public Page<UserMessageResponseDTO> getUsersMessages(@RequestBody List<UUID> ids,
            @RequestParam Map<String, String> params) {
        params = new HashMap<>(params);
        params.put("active", "true");
        Pageable pageable = pageableUtil.getPageable(
                params.getOrDefault("page", "1"),
                PageSize.USER_PAGE_SIZE,
                List.of());
        Page<User> users = userService.getUsersByIds(ids, params, pageable);

        return users.map(userMapper::toUserMessageResponseDTO);
    }

    @GetMapping("/{id}/exist")
    public Boolean checkStudentExistById(@PathVariable("id") UUID id) {
        try {
            User user = userService.getUserById(id);
            return user.getActive() && studentService.existStudentById(id);
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/current-user")
    public UserDetailsResponseDTO getCurrentUser() {
        return userMapper.toUserDetailsResponseDTO(userService.getCurrentUser());
    }

}
