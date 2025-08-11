package com.lqm.controllers;

import com.lqm.dtos.UserDTO;
import com.lqm.models.User;
import com.lqm.services.UserService;
import com.lqm.utils.JwtUtils;
import com.lqm.validators.WebAppValidator;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiUserController {

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("webAppValidator")
    private WebAppValidator webAppValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @Autowired
    private MessageSource messageSource;

    /**
     * Tạo mới user với multipart/form-data
     */
    @PostMapping(path = "/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(
            @ModelAttribute @Valid UserDTO userDTO,
            BindingResult result) {

        if (result.hasErrors()) {
            List<Map<String, String>> errors = result.getFieldErrors().stream()
                    .map(error -> Map.of(
                            "field", error.getField(),
                            "message", error.getDefaultMessage() != null
                                    ? error.getDefaultMessage()
                                    : messageSource.getMessage(
                                    Objects.requireNonNull(error.getCode()),
                                    null,
                                    Locale.getDefault())
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        // Tạo User entity từ DTO
        User user = userDTO.toEntity();
        user.setActive(true);

        User saved = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }


    /**
     * Đăng nhập, trả về JWT
     */
    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) throws Exception {
        String email = payload.get("email");
        String pwd = payload.get("password");
        if (userService.authenticate(email, pwd)) {
            User current = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found after auth"));
            String token = JwtUtils.generateToken(email, current.getRole());
            return ResponseEntity.ok().body(Collections.singletonMap("token", token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Sai thông tin đăng nhập");
    }

    /**
     * Lấy profile của user đang đăng nhập
     */
    @GetMapping("/secure/profile")
    public ResponseEntity<User> getProfile(Principal principal) {
        User user = userService.getUserByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
        return ResponseEntity.ok(user);
    }

    /**
     * Danh sách users có phân trang và lọc
     */
    @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<User>> listUsers(
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> pages = userService.getUsers(kw, role, pageable);
        return ResponseEntity.ok(pages);
    }
}
