package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.UserClient;
import com.lqm.admin_service.clients.YearClient;
import com.lqm.admin_service.configs.JwtConfig;
import com.lqm.admin_service.dtos.AcademicResponseDTO;
import com.lqm.admin_service.dtos.YearRequestDTO;
import com.lqm.admin_service.exceptions.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(YearController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "jwt.secret=mock-secret-key-that-is-at-least-32-bytes-long",
        "jwt.expiration-ms=3600000",
        "spring.cloud.openfeign.circuitbreaker.enabled=false"
})
class YearControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private YearClient yearClient;

    @MockitoBean
    private MessageSource messageSource;

    @MockitoBean
    private JwtConfig jwtConfig;

    @MockitoBean
    private UserClient userClient;

    @Test
    @DisplayName("GET /years: returns view with list of years")
    void listYears_ReturnsView() throws Exception {
        AcademicResponseDTO dto = mock(AcademicResponseDTO.class);
        Page<AcademicResponseDTO> page = new PageImpl<>(List.of(dto));
        when(yearClient.getYears(anyMap())).thenReturn(page);

        mockMvc.perform(get("/years").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("year/list"))
                .andExpect(model().attributeExists("years", "params"))
                .andExpect(model().attribute("years", page));
    }

    @Test
    @DisplayName("GET /years/add: shows add form")
    void showAddYearForm_ReturnsView() throws Exception {
        mockMvc.perform(get("/years/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("year/form"))
                .andExpect(model().attributeExists("year"));
    }

    @Test
    @DisplayName("GET /years/{id}: shows edit form with year data")
    void showEditYearForm_ReturnsView() throws Exception {
        UUID id = UUID.randomUUID();
        YearRequestDTO dto = YearRequestDTO.builder().build();
        when(yearClient.getYearRequestById(id)).thenReturn(dto);

        mockMvc.perform(get("/years/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("year/form"))
                .andExpect(model().attributeExists("year"));
    }

    @Test
    @DisplayName("POST /years: saves year and redirects")
    void saveYear_Success_Redirects() throws Exception {
        doNothing().when(yearClient).saveYear(any(YearRequestDTO.class));

        mockMvc.perform(post("/years")
                .param("name", "2024-2025"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/years"));
    }

    @Test
    @DisplayName("POST /years: handles validation exception from client")
    void saveYear_ValidationException_ShowsForm() throws Exception {
        Map<String, String> errors = Map.of("name", "Invalid year name");
        ValidationException exception = new ValidationException("Validation failed", errors);
        
        doThrow(exception).when(yearClient).saveYear(any(YearRequestDTO.class));

        mockMvc.perform(post("/years").param("name", "2024-2025"))
                .andExpect(status().isOk())
                .andExpect(view().name("year/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("POST /years: handles generic exception")
    void saveYear_GenericException_ShowsForm() throws Exception {
        doThrow(new RuntimeException("Server error")).when(yearClient).saveYear(any(YearRequestDTO.class));
        when(messageSource.getMessage(eq("error"), isNull(), any(Locale.class))).thenReturn("Generic error occurred");

        mockMvc.perform(post("/years").param("name", "2024-2025"))
                .andExpect(status().isOk())
                .andExpect(view().name("year/form"))
                .andExpect(model().attribute("errorMessage", "Generic error occurred"));
    }

    @Test
    @DisplayName("DELETE /years/{id}: returns 204")
    void deleteYear_Returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(yearClient).deleteYearById(id);

        mockMvc.perform(delete("/years/{id}", id))
                .andExpect(status().isNoContent());
    }
}
