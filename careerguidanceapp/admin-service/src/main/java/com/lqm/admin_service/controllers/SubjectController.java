package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.SubjectClient;
import com.lqm.admin_service.dtos.AcademicResponseDTO;
import com.lqm.admin_service.dtos.SubjectRequestDTO;
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

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/subjects")
public class SubjectController {

    private final SubjectClient subjectClient;
    private final MessageSource messageSource;

    @GetMapping("")
    public String listSubjects(Model model, @RequestParam Map<String, String> params) {
        Page<AcademicResponseDTO> subjectDTOPage = subjectClient.getSubjects(params);

        model.addAttribute("subjects", subjectDTOPage);
        model.addAttribute("params", params);

        return "subject/list";
    }

    @GetMapping("/add")
    public String addSubject(Model model) {
        model.addAttribute("subject", SubjectRequestDTO.builder().build());

        return "subject/form";
    }

    @GetMapping("/{id}")
    public String updateSubject(Model model, @PathVariable UUID id) {
        model.addAttribute("subject", subjectClient.getSubjectRequestById(id));

        return "subject/form";
    }

    @PostMapping("")
    public String saveSubject(@ModelAttribute("subject") @Valid SubjectRequestDTO dto,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "subject/form";
        }

        try {
            subjectClient.saveSubject(dto);
            return "redirect:/subjects";

        } catch (ValidationException e) {
            if (e.getDetails() instanceof Map<?, ?> errors) {
                errors.forEach((field, message) -> bindingResult.rejectValue(field.toString(), "error.subject",
                        message.toString()));
            }

            return "subject/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    messageSource.getMessage("error", null, Locale.getDefault()));

            return "subject/form";
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubject(@PathVariable UUID id) {
        subjectClient.deleteSubjectById(id);
    }
}
