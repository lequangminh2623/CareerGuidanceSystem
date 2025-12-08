package com.lqm.academic_service.controllers;

import com.lqm.academic_service.clients.SubjectClient;
import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.SubjectRequestDTO;
import com.lqm.academic_service.utils.PageSize;
import com.lqm.academic_service.utils.PageableUtil;
import com.lqm.academic_service.validators.WebAppValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/subjects")
public class SubjectController {

    private final SubjectClient subjectClient;
    private final WebAppValidator webAppValidator;
    private final PageableUtil pageableUtil;
    private final MessageSource messageSource;

    @InitBinder({"subject"})
    public void initFormBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @GetMapping("")
    public String listSubjects(Model model, @RequestParam Map<String, String> params) {

        Pageable pageable = pageableUtil.getPageable(
                params.get("page"),
                PageSize.SUBJECT_PAGE_SIZE,
                List.of("name:asc")
        );

        Page<AcademicResponseDTO> subjectPage =
                subjectClient.getSubjects(params, pageable);

        model.addAttribute("subjects", subjectPage.getContent());
        model.addAttribute("currentPage", params.getOrDefault("page", "1"));
        model.addAttribute("totalPages", subjectPage.getTotalPages());
        model.addAttribute("kw", params.get("kw"));

        return "subject/subject-list";
    }

    @GetMapping("/add")
    public String addSubject(Model model) {
        model.addAttribute("subject", SubjectRequestDTO.builder().build());
        return "subject/subject-form";
    }

    @GetMapping("/{id}")
    public String updateSubject(Model model, @PathVariable UUID id) {
        model.addAttribute("subject", subjectClient.getSubjectRequestById(id));
        return "subject/subject-form";
    }

    @PostMapping("")
    public String saveSubject(
            @ModelAttribute("subject") @Valid SubjectRequestDTO dto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "errorMessage",
                    messageSource.getMessage("error", null, Locale.getDefault())
            );
            return "subject/subject-form";
        }

        subjectClient.saveSubject(dto);
        return "redirect:/subjects";
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubject(@PathVariable UUID id) {
        subjectClient.deleteSubjectById(id);
    }
}
