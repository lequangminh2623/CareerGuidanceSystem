package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.UserClient;
import com.lqm.admin_service.configs.JwtConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "jwt.secret=mock-secret-key-that-is-at-least-32-bytes-long",
        "jwt.expiration-ms=3600000",
        "spring.cloud.openfeign.circuitbreaker.enabled=false"
})
class IndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private JwtConfig jwtConfig;

    @Test
    @DisplayName("GET /: returns index view and populates stats")
    void index_ReturnsView() throws Exception {
        Map<String, Object> growthMap = Map.of("2022", 100, "2023", 200, "2024", 300);
        Map<String, Object> stats = Map.of("studentGrowth", growthMap);
        
        when(userClient.getStats()).thenReturn(ResponseEntity.ok(stats));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("stats", "latestYear", "latestCount", "requestURI", "contextPath"))
                .andExpect(model().attribute("latestYear", "2024"))
                .andExpect(model().attribute("latestCount", 300));
    }
}
