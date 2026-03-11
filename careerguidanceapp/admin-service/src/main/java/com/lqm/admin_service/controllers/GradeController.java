package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.GradeClient;
import com.lqm.admin_service.clients.YearClient;
import com.lqm.admin_service.dtos.AcademicResponseDTO;
import com.lqm.admin_service.dtos.GradeRequestDTO;
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
public class GradeController {

    private final YearClient yearClient;
    private final GradeClient gradeClient;
    private final org.springframework.context.MessageSource messageSource;

    @GetMapping("/years/{yearId}/grades")
    public String listGrades(@PathVariable UUID yearId,
                             @RequestParam Map<String, String> params,
                             Model model) {
        AcademicResponseDTO year = yearClient.getYearResponseById(yearId);
        List<AcademicResponseDTO> grades = gradeClient.getGradesByYearId(yearId, params);

        model.addAttribute("yearDisplay", year);
        model.addAttribute("gradeDisplays", grades);
        model.addAttribute("params", params);

        return "grade/grade-list";
    }

    @GetMapping("/years/{yearId}/grades/add")
    public String showAddGradeForm(@PathVariable UUID yearId, Model model) {
        AcademicResponseDTO year = yearClient.getYearResponseById(yearId);
        GradeRequestDTO gradeRequestDTO = GradeRequestDTO.builder().yearId(yearId).build();

        model.addAttribute("grade", gradeRequestDTO);
        model.addAttribute("yearDisplay", year);

        return "grade/grade-form";
    }

    @GetMapping("/grades/{gradeId}")
    public String showEditGradeForm(@PathVariable UUID gradeId, Model model) {
        GradeRequestDTO grade = gradeClient.getGradeRequestById(gradeId);
        AcademicResponseDTO year = yearClient.getYearResponseById(grade.yearId());

        model.addAttribute("grade", grade);
        model.addAttribute("yearDisplay", year);

        return "grade/grade-form";
    }

    @PostMapping("/grades")
    public String saveGrade(@ModelAttribute("grade") @Valid GradeRequestDTO dto,
                            BindingResult bindingResult,
                            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("yearDisplay", yearClient.getYearResponseById(dto.yearId()));
            return "grade/grade-form";
        }

        try {
            gradeClient.saveGrade(dto);
            return "redirect:/years/" + dto.yearId() + "/grades";

        } catch (ValidationException e) {
            if (e.getDetails() instanceof Map<?, ?>) {
                Map<String, String> errors = (Map<String, String>) e.getDetails();

                errors.forEach((field, message) -> {
                    bindingResult.rejectValue(field, "error.grade", message);
                });
            }
            model.addAttribute("yearDisplay", yearClient.getYearResponseById(dto.yearId()));

            return "grade/grade-form";

        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    messageSource.getMessage("error", null, Locale.getDefault()));

            return "grade/grade-form";
        }
    }

    @DeleteMapping("/grades/{gradeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteGrade(@PathVariable UUID gradeId) {
        gradeClient.deleteGradeById(gradeId);

        return ResponseEntity.noContent().build();
    }
}
