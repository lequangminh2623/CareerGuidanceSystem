package com.lqm.user_service.controllers;

import com.lqm.user_service.dtos.UserDetailsResponseDTO;
import com.lqm.user_service.dtos.UserRequestDTO;
import com.lqm.user_service.dtos.UserResponseDTO;
import com.lqm.user_service.mappers.UserMapper;
import com.lqm.user_service.models.User;
import com.lqm.user_service.services.UserService;
import com.lqm.user_service.utils.PageSize;
import com.lqm.user_service.utils.PageableUtil;
import com.lqm.user_service.validators.WebAppValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/secure/users")
@CrossOrigin
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;
    private final PageableUtil pageableUtil;
    private final UserMapper userMapper;
    private final WebAppValidator webAppValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @GetMapping("/details")
    public Page<UserDetailsResponseDTO> getUsersDetails(
            @RequestParam Map<String, String> params,
            Pageable pageable
    ) {
        Page<User> users = userService.getUsers(List.of(), params, pageable);
        return users.map(userMapper::toUserDetailsResponseDTO);
    }

    @GetMapping("/{id}")
    public UserRequestDTO getUserRequestById(@PathVariable UUID id) {
        return userMapper.toUserRequestDTO(userService.getUserById(id));
    }

    @PostMapping(path = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDetailsResponseDTO saveUser(
            @SpringQueryMap UserRequestDTO userRequestDTO,
            @RequestPart("file") MultipartFile file
    ) {
        User user = userMapper.toEntity(userRequestDTO);
        User savedUser = userService.saveUser(user, file, userRequestDTO.getCode());
        return userMapper.toUserDetailsResponseDTO(savedUser);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable UUID id) {
        userService.deleteUser(id);
    }

    @PostMapping
    public Page<UserResponseDTO> getUsers(
            @RequestBody List<UUID> ids,
            @RequestParam Map<String, String> params
    ) {
        Pageable pageable = pageableUtil.getPageable(
                params.get("page"),
                PageSize.USER_PAGE_SIZE,
                List.of("lastName:asc", "firstName:asc")
        );
        Page<User> userPage = userService.getUsers(ids, params, pageable);
        return userPage.map(userMapper::toUserResponseDTO);
    }

}
