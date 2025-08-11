package com.lqm.controllers;

import com.lqm.models.Student;
import com.lqm.models.User;
import com.lqm.services.UserService;
import com.lqm.validators.WebAppValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("webAppValidator")
    private WebAppValidator webAppValidator;

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
    public String listUsers(
            Model model,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("lastName").ascending());
        Page<User> userPage = userService.getUsers(kw, role, pageable);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", userPage.getNumber() + 1);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("kw", kw);
        model.addAttribute("role", role);
        return "/user/user-list";
    }

    @GetMapping("/users/add")
    public String addUser(Model model) {
        User user = new User();
        user.setStudent(new Student());
        model.addAttribute("user", user);
        return "/user/user-form";
    }

    @PostMapping("/users")
    public String saveUser(
            @ModelAttribute("user") @Valid User user,
            BindingResult bindingResult,
            Model model
    ) {
        if ("ROLE_STUDENT".equals(user.getRole())) {
            Student s = user.getStudent();
            s.setUser(user);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra");
            return "/user/user-form";
        }

        if (!"ROLE_STUDENT".equals(user.getRole())) {
            user.setStudent(null);
        }

        userService.saveUser(user);
        return "redirect:/users";
    }

    @GetMapping("/users/{id}")
    public String updateUser(
            @PathVariable Integer id,
            Model model
    ) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found id: " + id));
        model.addAttribute("user", user);
        return "/user/user-form";
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
    }
}
