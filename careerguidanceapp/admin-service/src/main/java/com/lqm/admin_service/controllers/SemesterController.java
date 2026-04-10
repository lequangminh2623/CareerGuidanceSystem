package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.SemesterClient;
import com.lqm.admin_service.clients.YearClient;
import com.lqm.admin_service.dtos.AcademicResponseDTO;
import com.lqm.admin_service.dtos.SemesterRequestDTO;
import com.lqm.admin_service.exceptions.ValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class SemesterController {

    private final YearClient yearClient;
    private final SemesterClient semesterClient;
    private final org.springframework.context.MessageSource messageSource;

    @GetMapping("/years/{yearId}/semesters")
    public String listSemesters(@PathVariable UUID yearId,
            @RequestParam Map<String, String> params,
            Model model) {
        AcademicResponseDTO year = yearClient.getYearResponseById(yearId);
        List<AcademicResponseDTO> semesters = semesterClient.getSemestersByYearId(yearId, params);

        model.addAttribute("yearDisplay", year);
        model.addAttribute("semesterDisplays", semesters);
        model.addAttribute("params", params);

        return "semester/list";
    }

    @GetMapping("/years/{yearId}/semesters/add")
    public String showAddSemesterForm(@PathVariable UUID yearId, Model model) {
        AcademicResponseDTO year = yearClient.getYearResponseById(yearId);
        SemesterRequestDTO semesterRequestDTO = SemesterRequestDTO.builder().yearId(yearId).build();

        model.addAttribute("semester", semesterRequestDTO);
        model.addAttribute("yearDisplay", year);

        return "semester/form";
    }

    @GetMapping("/semesters/{semesterId}")
    public String showEditSemesterForm(@PathVariable UUID semesterId, Model model) {
        SemesterRequestDTO semester = semesterClient.getSemesterRequestById(semesterId);
        AcademicResponseDTO year = yearClient.getYearResponseById(semester.yearId());

        model.addAttribute("semester", semester);
        model.addAttribute("yearDisplay", year);

        return "semester/form";
    }

    @PostMapping("/semesters")
    public String saveSemester(@ModelAttribute("semester") @Valid SemesterRequestDTO dto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("yearDisplay", yearClient.getYearResponseById(dto.yearId()));
            return "semester/form";
        }

        try {
            semesterClient.saveSemester(dto);
            return "redirect:/years/" + dto.yearId() + "/semesters";

        } catch (ValidationException e) {
            if (e.getDetails() instanceof Map<?, ?> errors) {
                errors.forEach((field, message) -> {
                    try {
                        bindingResult.rejectValue(field.toString(), "error.semester", message.toString());
                    } catch (Exception ex) {
                        bindingResult.reject("error.global", message.toString());
                    }
                });
            }

            model.addAttribute("yearDisplay", yearClient.getYearResponseById(dto.yearId()));
            return "semester/form";

        } catch (Exception e) {
            model.addAttribute("yearDisplay", yearClient.getYearResponseById(dto.yearId()));
            model.addAttribute("errorMessage", messageSource.getMessage("error", null, Locale.getDefault()));
            return "semester/form";
        }
    }

    @DeleteMapping("/semesters/{semesterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteSemester(@PathVariable UUID semesterId) {
        semesterClient.deleteSemesterById(semesterId);

        return ResponseEntity.noContent().build();
    }
}
