package com.lqm.admin_service.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@ControllerAdvice(basePackages = "com.lqm.admin_service.controllers")
public class BaseController {
    @ModelAttribute("currentUriBuilder")
    public ServletUriComponentsBuilder currentUriBuilder() {
        return ServletUriComponentsBuilder.fromCurrentRequest();
    }
}
