package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.UserClient;
import com.lqm.admin_service.dtos.UserDetailsResponseDTO;
import com.lqm.admin_service.dtos.AdminUserRequestDTO;
import com.lqm.admin_service.exceptions.ValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserClient userClient;
//    private final WebAppValidator webAppValidator;
    private final MessageSource messageSource;

//    @InitBinder
//    public void initBinder(WebDataBinder binder) {
//        binder.setValidator(webAppValidator);
//    }

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

        Page<UserDetailsResponseDTO> userDTOPage = userClient.getUsersDetails(params);

        model.addAttribute("users", userDTOPage);
        model.addAttribute("params", params);

        return "user/list";
    }

    @GetMapping("/users/add")
    public String addUser(Model model) {
        model.addAttribute("user", AdminUserRequestDTO.builder().build());

        return "user/form";
    }

    @GetMapping("/users/{id}")
    public String updateUser(@PathVariable UUID id, Model model) {

        AdminUserRequestDTO userDTO = userClient.getUserRequestById(id);

        model.addAttribute("user", userDTO);

        return "user/form";
    }

    @PostMapping(path = "/users/save")
    public String saveUser(@ModelAttribute("user") @Valid AdminUserRequestDTO user,
                           BindingResult bindingResult,
                           @RequestPart(value = "file", required = false) MultipartFile file,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "user/form";
        }

        try {
            userClient.saveUser(user, file);
            return "redirect:/users";
        } catch (ValidationException e) {
            if (e.getDetails() instanceof Map<?, ?> errors) {
                errors.forEach((field, message) -> {
                    bindingResult.rejectValue(field.toString(), "error.user", message.toString());
                });
            }

            return "user/form";

        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    messageSource.getMessage("error", null, Locale.getDefault()));

            return "user/form";
        }
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        userClient.deleteUserById(id);
    }
}
