package com.lqm.user_service.controllers;

import com.lqm.user_service.dtos.UserDetailsResponseDTO;
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
                List.of()
        );
        Page<User> userPage = userService.getUsers(ids, params, pageable);

        return userPage.map(userMapper::toUserResponseDTO);
    }

    @GetMapping("/details")
    public Page<UserDetailsResponseDTO> getUsersDetails(@RequestBody List<UUID> ids,
                                                        @RequestParam Map<String, String> params) {
        Pageable pageable = pageableUtil.getPageable(
                params.getOrDefault("page", "1"),
                PageSize.USER_PAGE_SIZE,
                List.of()
        );
        Page<User> users = userService.getUsers(ids, params, pageable);

        return users.map(userMapper::toUserDetailsResponseDTO);
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable("id") UUID id) {
        return userMapper.toUserResponseDTO(userService.getUserById(id));
    }

    @GetMapping("/{id}/exist")
    public Boolean checkStudentExistById(@PathVariable("id") UUID id) {
        return studentService.existStudentById(id);
    }

    @GetMapping("/current_user")
    public UserResponseDTO getCurrentUser() {
        return userMapper.toUserResponseDTO(userService.getCurrentUser());
    }

}
