package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.YearClient;
import com.lqm.admin_service.dtos.AcademicResponseDTO;
import com.lqm.admin_service.dtos.YearRequestDTO;
import com.lqm.admin_service.exceptions.ValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class YearController {

    private final YearClient yearClient;
    private final MessageSource messageSource;

    @GetMapping("/years")
    public String listYears(Model model, @RequestParam Map<String, String> params) {
        Page<AcademicResponseDTO> yearDTOPage = yearClient.getYears(params);

        model.addAttribute("years", yearDTOPage);
        model.addAttribute("params", params);

        return "year/list";
    }

    @GetMapping("/years/add")
    public String showAddYearForm(Model model) {
        model.addAttribute("year", YearRequestDTO.builder().build());

        return "year/form";
    }

    @GetMapping("/years/{id}")
    public String showEditYearForm(@PathVariable UUID id, Model model) {
        model.addAttribute("year", yearClient.getYearRequestById(id));

        return "year/form";
    }

    @PostMapping("/years")
    public String saveYear(@ModelAttribute("year") @Valid YearRequestDTO yearRequestDTO,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "year/form";
        }

        try {
            yearClient.saveYear(yearRequestDTO);
            return "redirect:/years";

        } catch (ValidationException e) {
            if (e.getDetails() instanceof Map<?, ?> errors) {
                errors.forEach((field, message) -> {
                    bindingResult.rejectValue(field.toString(), "error.year", message.toString());
                });
            }

            return "year/form";

        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    messageSource.getMessage("error", null, Locale.getDefault()));

            return "year/form";
        }
    }

    @DeleteMapping("/years/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteYear(@PathVariable UUID id) {
        yearClient.deleteYearById(id);

        return ResponseEntity.noContent().build();
    }
}
