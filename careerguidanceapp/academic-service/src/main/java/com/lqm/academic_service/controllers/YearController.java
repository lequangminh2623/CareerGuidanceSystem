package com.lqm.academic_service.controllers;

import com.lqm.academic_service.clients.GradeClient;
import com.lqm.academic_service.clients.SemesterClient;
import com.lqm.academic_service.clients.YearClient;
import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.GradeRequestDTO;
import com.lqm.academic_service.dtos.SemesterRequestDTO;
import com.lqm.academic_service.dtos.YearRequestDTO;
import com.lqm.academic_service.exceptions.DataConflictException;
import com.lqm.academic_service.validators.WebAppValidator;
import com.lqm.academic_service.utils.PageSize;
import com.lqm.academic_service.utils.PageableUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class YearController {

    private final YearClient yearClient;
    private final SemesterClient semesterClient;
    private final GradeClient gradeClient;
    private final WebAppValidator webAppValidator;
    private final PageableUtil pageableUtil;
    private final MessageSource messageSource;

    @InitBinder({"year", "semester", "grade"})
    public void initFormBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    // --- Year endpoints ---

    @GetMapping("/years")
    public String listYears(Model model, @RequestParam Map<String, String> params) {
        Pageable pageable = pageableUtil.getPageable(
                params.get("page"),
                PageSize.YEAR_PAGE_SIZE,
                List.of("name:desc")
        );

        Page<AcademicResponseDTO> yearPage = yearClient.getYears(params, pageable);

        model.addAttribute("years", yearPage.getContent());
        model.addAttribute("currentPage", params.get("page") != null ? Integer.parseInt(params.get("page")) : 1);
        model.addAttribute("totalPages", yearPage.getTotalPages());
        model.addAttribute("kw", params.get("kw"));
        return "year/year-list";
    }

    @GetMapping("/years/add")
    public String showAddYearForm(Model model) {
        model.addAttribute("year", YearRequestDTO.builder().build());
        return "year/year-form";
    }

    @GetMapping("/years/{id}")
    public String showEditYearForm(@PathVariable UUID id, Model model) {
        YearRequestDTO year = yearClient.getYearRequestById(id);
        model.addAttribute("year", year);
        return "year/year-form";
    }

    @PostMapping("/years")
    public String saveYear(@ModelAttribute("year") @Valid YearRequestDTO yearRequestDTO,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage",
                    messageSource.getMessage("error", null, Locale.getDefault())
            );
            return "year/year-form";
        }

        yearClient.saveYear(yearRequestDTO);
        return "redirect:/years";
    }

    @DeleteMapping("/years/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteYear(@PathVariable UUID id) {
        try {
            yearClient.deleteYearById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException(
                    messageSource.getMessage("year.delete.error", null, Locale.getDefault())
            );
        } catch (Exception e) {
            throw new RuntimeException(messageSource.getMessage("error", null, Locale.getDefault()), e);
        }
    }

    // --- Semester nested under year ---

    @GetMapping("/years/{yearId}/semesters")
    public String listSemesters(@PathVariable UUID yearId,
                                @RequestParam Map<String, String> params,
                                Model model) {
        AcademicResponseDTO year = yearClient.getYearResponseById(yearId);
        List<AcademicResponseDTO> semesters = semesterClient.getSemestersByYearId(yearId, params);

        model.addAttribute("yearDisplay", year);
        model.addAttribute("semesterDisplays", semesters);
        model.addAttribute("kw", params.get("kw"));
        return "year/semester-list";
    }

    @GetMapping("/years/{yearId}/semesters/add")
    public String showAddSemesterForm(@PathVariable UUID yearId, Model model) {
        AcademicResponseDTO year = yearClient.getYearResponseById(yearId);
        SemesterRequestDTO semesterRequestDTO = SemesterRequestDTO.builder().yearId(yearId).build();
        model.addAttribute("semester", semesterRequestDTO);
        model.addAttribute("yearDisplay", year);
        return "year/semester-form";
    }

    @GetMapping("/semesters/{semesterId}")
    public String showEditSemesterForm(@PathVariable UUID semesterId, Model model) {
        SemesterRequestDTO semester = semesterClient.getSemesterRequestById(semesterId);
        AcademicResponseDTO year = yearClient.getYearResponseById(semester.yearId());
        model.addAttribute("semester", semester);
        model.addAttribute("yearDisplay", year);
        return "year/semester-form";
    }

    @PostMapping("/semesters")
    public String saveSemester(@ModelAttribute("semester") @Valid SemesterRequestDTO dto,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            AcademicResponseDTO year = yearClient.getYearResponseById(dto.yearId());
            model.addAttribute("errorMessage",
                    messageSource.getMessage("error", null, Locale.getDefault()));
            model.addAttribute("yearDisplay", year);
            return "year/semester-form";
        }
        semesterClient.saveSemester(dto);
        return "redirect:/years/" + dto.yearId() + "/semesters";
    }

    @DeleteMapping("/semesters/{semesterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteSemester(@PathVariable UUID semesterId) {
        try {
            semesterClient.deleteSemesterById(semesterId);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException(
                    messageSource.getMessage("semester.delete.error", null, Locale.getDefault())
            );
        } catch (Exception e) {
            throw new RuntimeException(messageSource.getMessage("error", null, Locale.getDefault()), e);
        }
    }

    // --- Grade nested under year ---

    @GetMapping("/years/{yearId}/grades")
    public String listGrades(@PathVariable UUID yearId,
                             @RequestParam Map<String, String> params,
                             Model model) {
        AcademicResponseDTO year = yearClient.getYearResponseById(yearId);
        List<AcademicResponseDTO> grades = gradeClient.getGradesByYearId(yearId, params);

        model.addAttribute("yearDisplay", year);
        model.addAttribute("gradeDisplays", grades);
        model.addAttribute("kw", params.get("kw"));
        return "year/grade-list";
    }

    @GetMapping("/years/{yearId}/grades/add")
    public String showAddGradeForm(@PathVariable UUID yearId, Model model) {
        AcademicResponseDTO year = yearClient.getYearResponseById(yearId);
        GradeRequestDTO gradeRequestDTO = GradeRequestDTO.builder().yearId(yearId).build();
        model.addAttribute("grade", gradeRequestDTO);
        model.addAttribute("yearDisplay", year);
        return "year/grade-form";
    }

    @GetMapping("/grades/{gradeId}")
    public String showEditGradeForm(@PathVariable UUID gradeId, Model model) {
        GradeRequestDTO grade = gradeClient.getGradeRequestById(gradeId);
        AcademicResponseDTO year = yearClient.getYearResponseById(grade.yearId());
        model.addAttribute("grade", grade);
        model.addAttribute("yearDisplay", year);
        return "year/grade-form";
    }

    @PostMapping("/grades")
    public String saveGrade(@ModelAttribute("grade") @Valid GradeRequestDTO dto,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            AcademicResponseDTO year = yearClient.getYearResponseById(dto.yearId());
            model.addAttribute("errorMessage",
                    messageSource.getMessage("error", null, Locale.getDefault()));
            model.addAttribute("yearDisplay", year);
            return "year/grade-form";
        }
        gradeClient.saveGrade(dto);
        return "redirect:/years/" + dto.yearId() + "/grades";
    }

    @DeleteMapping("/grades/{gradeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteGrade(@PathVariable UUID gradeId) {
        try {
            gradeClient.deleteGradeById(gradeId);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException(
                    messageSource.getMessage("grade.delete.error", null, Locale.getDefault())
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    messageSource.getMessage("error", null, Locale.getDefault()), e
            );
        }
    }
}
