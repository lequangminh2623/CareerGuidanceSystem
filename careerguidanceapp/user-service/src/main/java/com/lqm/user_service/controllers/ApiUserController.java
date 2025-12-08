package com.lqm.user_service.controllers;

import com.lqm.user_service.dtos.UserResponseDTO;
import com.lqm.user_service.dtos.UserDetailsResponseDTO;
import com.lqm.user_service.mappers.UserMapper;
import com.lqm.user_service.models.User;
import com.lqm.user_service.services.UserService;
import com.lqm.user_service.utils.PageSize;
import com.lqm.user_service.utils.PageableUtil;
import com.lqm.user_service.validators.WebAppValidator;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/secure")
@CrossOrigin
@RequiredArgsConstructor
public class ApiUserController {

    private final UserService userService;
    private final WebAppValidator webAppValidator;
    private final PageableUtil pageableUtil;
    private final UserMapper userMapper;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDetailsResponseDTO> getProfile(HttpServletRequest request) {
        User user = userService.getCurrentUser(request);
        UserDetailsResponseDTO userResponseDTO = userMapper.toUserDetailsResponseDTO(user);
        return ResponseEntity.ok(userResponseDTO);
    }


    //đường dẫn thay đổi
    @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<UserResponseDTO>> listUsers(
            @RequestParam Map<String, String> params
    ) {
        Pageable pageable = pageableUtil.getPageable(params.get("page"), PageSize.USER_PAGE_SIZE, List.of("lastName:asc", "firstName:asc"));

        Page<User> pages = userService.getUsers(List.of(), params, pageable);

        return ResponseEntity.ok(pages.map(userMapper::toUserResponseDTO));
    }

}

