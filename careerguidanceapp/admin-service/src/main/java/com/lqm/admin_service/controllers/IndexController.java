package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.UserClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

@Controller
@ControllerAdvice
@RequiredArgsConstructor
public class IndexController {

    public final UserClient userClient;

    @ModelAttribute
    public void requestURI(HttpServletRequest request, Model model) {
        model.addAttribute("requestURI", request.getRequestURI());
    }

    @ModelAttribute
    public void contextPath(HttpServletRequest request, Model model) {
        model.addAttribute("contextPath", request.getContextPath());
    }

    @GetMapping("/")
    public String index(Model model) {
        ResponseEntity<Map<String, Object>> response = userClient.getStats();

        if (response != null && response.getBody() != null) {
            Map<String, Object> stats = response.getBody();

            // Lấy Map studentGrowth từ response
            @SuppressWarnings("unchecked")
            Map<String, Object> growthMap = (Map<String, Object>) stats.get("studentGrowth");

            String latestYear = "0";
            Object latestCount = 0;

            if (growthMap != null && !growthMap.isEmpty()) {
                // Tìm năm lớn nhất (Key lớn nhất)
                latestYear = growthMap.keySet().stream()
                        .map(String::valueOf)
                        .max(String::compareTo)
                        .orElse("0");
                latestCount = growthMap.get(latestYear);
            }

            model.addAttribute("stats", stats);
            model.addAttribute("latestYear", latestYear);
            model.addAttribute("latestCount", latestCount);
        }

        return "index";
    }

}
