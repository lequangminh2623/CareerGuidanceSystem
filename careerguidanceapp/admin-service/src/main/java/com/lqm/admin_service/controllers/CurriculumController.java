package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.CurriculumClient;
import com.lqm.admin_service.clients.GradeClient;
import com.lqm.admin_service.clients.SemesterClient;
import com.lqm.admin_service.clients.SubjectClient;
import com.lqm.admin_service.dtos.*;
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

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/curriculums")
public class CurriculumController {

    private final CurriculumClient curriculumClient;
    private final MessageSource messageSource;
    private final GradeClient gradeClient;
    private final SubjectClient subjectClient;
    private final SemesterClient semesterClient;

    @ModelAttribute("subjects")
    public List<AcademicResponseDTO> populateSubjects() {
        return subjectClient.getSubjects(Map.of()).getContent();
    }

    @ModelAttribute("params")
    public Map<String, String> populateParams(@RequestParam Map<String, String> params) {
        return params;
    }

    @ModelAttribute("semesters")
    public List<AcademicResponseDTO> populateSemesters(@RequestParam Map<String, String> params) {
        String gradeIdStr = params.get("gradeId");
        if (gradeIdStr == null || gradeIdStr.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            UUID gradeId = UUID.fromString(gradeIdStr);
            var gradeDetail = gradeClient.getGradeRequestById(gradeId);
            return semesterClient.getSemestersByYearId(gradeDetail.yearId(), Map.of());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @ModelAttribute("groupedGrades")
    public Map<String, List<GradeDetailsResponseDTO>> groupedGrades() {
        List<GradeDetailsResponseDTO> rawGrades = gradeClient.getGradesDetails(Map.of());

        return rawGrades.stream()
                .collect(Collectors.groupingBy(
                        GradeDetailsResponseDTO::yearName,
                        () -> new TreeMap<>(Comparator.reverseOrder()),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    list.sort(Comparator.comparing(GradeDetailsResponseDTO::name));
                                    return list;
                                }
                        )
                ));
    }

    @GetMapping("")
    public String listCurriculums(Model model, @RequestParam Map<String, String> params) {
        Page<CurriculumResponseDTO> curriculumDTOPage = curriculumClient.getCurriculums(params);

        model.addAttribute("curriculums", curriculumDTOPage);

        return "curriculum/list";
    }

    @GetMapping("/add")
    public String addCurriculum(Model model) {
        model.addAttribute("curriculum", CurriculumRequestDTO.builder().build());
        return "curriculum/form";
    }

    @GetMapping("/{id}")
    public String updateCurriculum(@PathVariable UUID id, Model model) {
        model.addAttribute("curriculum", curriculumClient.getCurriculumRequestById(id));
        return "curriculum/form";
    }

    @PostMapping
    public String saveCurriculum(@Valid @ModelAttribute("curriculum") CurriculumRequestDTO curriculumRequestDTO,
                                 BindingResult bindingResult,
                                 Model model) {

        if (bindingResult.hasErrors()) {
            return "curriculum/form";
        }

        try {
            curriculumClient.saveCurriculum(curriculumRequestDTO);
            return "redirect:/curriculums";

        } catch (ValidationException e) {
            if (e.getDetails() instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, String> errors = (Map<String, String>) e.getDetails();

                errors.forEach((field, message) -> {
                    bindingResult.rejectValue(field, "error.curriculum", message);
                });
            }
            return "curriculum/form";

        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    messageSource.getMessage("error", null, Locale.getDefault()));

            return "curriculum/form";
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurriculum(@PathVariable UUID id) {
        curriculumClient.deleteCurriculum(id);
    }
}
