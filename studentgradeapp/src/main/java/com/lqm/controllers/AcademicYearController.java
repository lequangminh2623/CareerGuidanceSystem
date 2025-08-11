package com.lqm.controllers;

import com.lqm.models.AcademicYear;
import com.lqm.models.Semester;
import com.lqm.services.AcademicYearService;
import com.lqm.services.SemesterService;
import com.lqm.utils.PageSize;
import com.lqm.validators.WebAppValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class AcademicYearController {

    private final AcademicYearService academicYearService;
    private final SemesterService semesterService;
    private final WebAppValidator webAppValidator;

    @Autowired
    public AcademicYearController(AcademicYearService academicYearService,
                                  SemesterService semesterService,
                                  @Qualifier("webAppValidator") WebAppValidator webAppValidator) {
        this.academicYearService = academicYearService;
        this.semesterService = semesterService;
        this.webAppValidator = webAppValidator;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    // --- AcademicYear endpoints ---

    @GetMapping("/years")
    public String listYears(Model model,
                            @RequestParam Map<String, String> params) {
        int page = 1;
        if (params.get("page") != null) {
            page = Integer.parseInt(params.get("page"));
        }
        Pageable pageable = PageRequest.of(page - 1, PageSize.YEAR_PAGE_SIZE);
        Page<AcademicYear> yearPage = academicYearService.getAcademicYears(params, pageable);

        model.addAttribute("years", yearPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", yearPage.getTotalPages());
        model.addAttribute("kw", params.get("kw"));
        return "/year/year-list";
    }

    @GetMapping("/years/add")
    public String showAddYearForm(Model model) {
        model.addAttribute("academicYear", new AcademicYear());
        return "/year/year-form";
    }

    @PostMapping("/years")
    public String saveYear(@ModelAttribute("academicYear") @Valid AcademicYear academicYear,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Vui lòng kiểm tra dữ liệu năm học");
            return "/year/year-form";
        }
        academicYearService.saveYear(academicYear);
        return "redirect:/years";
    }

    @GetMapping("/years/{id}")
    public String showEditYearForm(@PathVariable int id,
                                   Model model) {
        AcademicYear year = academicYearService.getYearById(id);
        model.addAttribute("academicYear", year);
        return "/year/year-form";
    }

    @DeleteMapping("/years/{id}")
    public ResponseEntity<String> deleteYear(@PathVariable int id) {
        try {
            academicYearService.deleteYearById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Không thể xóa năm học này vì ràng buộc dữ liệu");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    // --- Semester nested under AcademicYear ---

    @GetMapping("/years/{yearId}/semesters")
    public String listSemesters(@PathVariable int yearId,
                                @RequestParam Map<String, String> params,
                                Model model) {
        AcademicYear year = academicYearService.getYearById(yearId);
        if (year != null) {
            model.addAttribute("year", year);
            model.addAttribute("semesters", semesterService.getSemestersByAcademicYearId(yearId, params));
            model.addAttribute("kwSem", params.get("kw"));
        }
        return "/year/semester-list";
    }

    @GetMapping("/years/{yearId}/semesters/add")
    public String showAddSemesterForm(@PathVariable int yearId,
                                      Model model) {
        AcademicYear year = academicYearService.getYearById(yearId);
        if (year != null) {
            Semester sem = new Semester();
            sem.setAcademicYear(year);
            model.addAttribute("semester", sem);
            model.addAttribute("year", year);
        }
        return "/year/semester-form";
    }

    @PostMapping("/years/{yearId}/semesters")
    public String saveSemester(@PathVariable int yearId,
                               @ModelAttribute("semester") @Valid Semester semester,
                               BindingResult bindingResult,
                               Model model) {
        AcademicYear year = academicYearService.getYearById(yearId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Vui lòng kiểm tra dữ liệu học kỳ");
            model.addAttribute("year", year);
            return "/year/semester-form";
        }
        semester.setAcademicYear(year);
        semesterService.saveSemester(semester);
        return "redirect:/years/{yearId}/semesters";
    }

    @GetMapping("/years/{yearId}/semesters/{semesterId}")
    public String showEditSemesterForm(@PathVariable int yearId,
                                       @PathVariable int semesterId,
                                       Model model) {
        AcademicYear year = academicYearService.getYearById(yearId);
        if (year != null) {
            Semester sem = semesterService.getSemesterById(semesterId);
            sem.setAcademicYear(year);
            model.addAttribute("semester", sem);
            model.addAttribute("year", year);
        }
        return "/year/semester-form";
    }

    @DeleteMapping("/years/{yearId}/semesters/{semesterId}")
    public ResponseEntity<String> deleteSemester(@PathVariable int semesterId) {
        try {
            semesterService.deleteSemesterById(semesterId);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Không thể xóa học kỳ này vì ràng buộc dữ liệu");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Đã xảy ra lỗi: " + e.getMessage());
        }
    }
}
