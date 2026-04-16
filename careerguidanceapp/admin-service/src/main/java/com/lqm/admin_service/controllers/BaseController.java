package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.UserClient;
import com.lqm.admin_service.dtos.UserDetailsResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@ControllerAdvice(basePackages = "com.lqm.admin_service.controllers")
@RequiredArgsConstructor
public class BaseController {

    private final UserClient userClient;

    @ModelAttribute("currentUriBuilder")
    public ServletUriComponentsBuilder currentUriBuilder() {
        return ServletUriComponentsBuilder.fromCurrentRequest();
    }

    @ModelAttribute("currentUser")
    public UserDetailsResponseDTO currentUser(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("/login") || uri.contains("/signup")) {
            return null;
        }

        try {
            return userClient.getCurrentUser();
        } catch (Exception e) {
            log.warn("Failed to get current user: {}", e.getMessage());
            return null;
        }
    }
}
