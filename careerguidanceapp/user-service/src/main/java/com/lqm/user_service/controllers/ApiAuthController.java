package com.lqm.user_service.controllers;

import com.lqm.user_service.dtos.UserLoginDTO;
import com.lqm.user_service.dtos.UserDetailsResponseDTO;
import com.lqm.user_service.dtos.UserRequestDTO;
import com.lqm.user_service.mappers.UserMapper;
import com.lqm.user_service.models.User;
import com.lqm.user_service.services.AuthService;
import com.lqm.user_service.services.UserService;
import com.lqm.user_service.validators.WebAppValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final AuthService authService;
    private final UserService userService;
    private final WebAppValidator webAppValidator;
    private final UserMapper userMapper;

    @InitBinder({ "userLoginDTO", "data" })
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> loginWithGoogle(@RequestBody Map<String, String> body) {
        Map<String, Object> result = authService.loginWithGoogle(body.get("token"));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginDTO userLoginDTO) throws Exception {
        String token = authService.login(userLoginDTO);
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }

    @PostMapping(path = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> signup(
            @RequestPart("data") @Valid UserRequestDTO userRequestDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        User user = userMapper.toEntity(userRequestDTO);
        User savedUser = userService.saveUser(user, file, userRequestDTO.code());
        UserDetailsResponseDTO userDetailsResponseDTO = userMapper.toUserDetailsResponseDTO(savedUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(userDetailsResponseDTO);
    }

}
