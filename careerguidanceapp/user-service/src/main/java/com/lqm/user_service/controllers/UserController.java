package com.lqm.user_service.controllers;

import com.lqm.user_service.annotations.MultipartNotNull;
import com.lqm.user_service.clients.UserClient;
import com.lqm.user_service.dtos.UserDetailsResponseDTO;
import com.lqm.user_service.dtos.UserRequestDTO;
import com.lqm.user_service.utils.PageSize;
import com.lqm.user_service.utils.PageableUtil;
import com.lqm.user_service.validators.WebAppValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserClient userClient;
    private final WebAppValidator webAppValidator;
    private final PageableUtil pageableUtil;
    private final MessageSource messageSource;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @GetMapping("/login")
    public String loginView() {
        return "login";
    }

    @GetMapping("/access-deny")
    public String getDenyPage() {
        return "deny";
    }

    @GetMapping("/users")
    public String listUsers(Model model, @RequestParam Map<String, String> params) {

        Pageable pageable = pageableUtil.getPageable(
                params.get("page"),
                PageSize.USER_PAGE_SIZE,
                List.of("lastName:acs")
        );

        Page<UserDetailsResponseDTO> userPage =
                userClient.getUsersDetails(params, pageable);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage",
                params.get("page") != null ? Integer.parseInt(params.get("page")) : 1);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("kw", params.get("kw"));

        return "user/user-list";
    }

    @GetMapping("/users/add")
    public String addUser(Model model) {
        model.addAttribute("user", UserRequestDTO.builder().build());
        return "user/user-form";
    }

    @GetMapping("/users/{id}")
    public String updateUser(@PathVariable UUID id, Model model) {

        UserRequestDTO dto = userClient.getUserRequestById(id);

        model.addAttribute("user", dto);

        return "user/user-form";
    }

    @PostMapping(path = "/users/save")
    public String saveUser(
            @ModelAttribute @Valid UserRequestDTO userRequestDTO,
            @RequestPart(value = "file") @MultipartNotNull MultipartFile file,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage",
                    messageSource.getMessage("error", null, Locale.getDefault())
            );
            return "user/user-form";
        }

        userClient.saveUser(userRequestDTO, file);

        return "redirect:/users";
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        userClient.deleteUserById(id);
    }
}
