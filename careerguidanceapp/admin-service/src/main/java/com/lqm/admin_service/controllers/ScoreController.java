package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.ClassroomClient;
import com.lqm.admin_service.clients.TranscriptClient;
import com.lqm.admin_service.clients.SectionClient;
import com.lqm.admin_service.dtos.*;
import com.lqm.admin_service.exceptions.ValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/transcripts")
@RequiredArgsConstructor
public class ScoreController {

    private final TranscriptClient transcriptClient;
    private final SectionClient sectionClient;
    private final ClassroomClient classroomClient;
    private final MessageSource messageSource;

    @GetMapping("/{id}")
    public String getTranscriptDetails(@PathVariable("id") UUID id, Model model, @RequestParam Map<String, String> params) {
        populateModel(model, params, id);

        return "/transcript/transcript-form";
    }

    @PostMapping("/{id}")
    public String saveScores(@PathVariable("id") UUID id,
                             @ModelAttribute("scores") @Valid ScoreListRequest request,
                             BindingResult bindingResult,
                             Model model,
                             @RequestParam Map<String, String> params) {

        if (bindingResult.hasErrors()) {
            populateModel(model, params, id);
            return "/transcript/transcript-form";
        }

        try {
            transcriptClient.saveScores(request, id);
            return "redirect:/classrooms/" + params.get("classroomId") + "/sections" + "?gradeId=" + params.get("gradeId");

        } catch (ValidationException e) {
            if (e.getDetails() instanceof Map<?, ?>) {
                Map<String, String> errors = (Map<String, String>) e.getDetails();
                errors.forEach((field, message) -> {
                    bindingResult.rejectValue(field, "error.transcript", message);
                });
            }
            populateModel(model, params, id);
            return "/transcript/transcript-form";

        } catch (Exception e) {
            model.addAttribute("errorMessage", messageSource.getMessage("error", null, Locale.getDefault()));
            populateModel(model, params, id);
            return "/transcript/transcript-form";
        }

    }

    @PatchMapping("/{id}/change-status")
    void changeTranscriptStatus(@PathVariable("id") UUID id, @Valid @RequestBody ChangeStatusRequestDTO request) {
        sectionClient.changeTranscriptStatus(id, request);
    }

    private void populateModel(Model model, Map<String, String> params, UUID id) {
        SectionResponseDTO section= sectionClient.getSectionResponseById(id);
        List<ScoreRequestDTO> scores = transcriptClient.getScoreRequests(id, Map.of());
        Map<UUID, UserResponseDTO> userMap = classroomClient.getStudentsInClassroom(section.classroomId(), Map.of())
                .getContent().stream().collect(Collectors.toMap(UserResponseDTO::id, Function.identity()));

        model.addAttribute("section", section);
        model.addAttribute("scores", scores);
        model.addAttribute("students", userMap);
        model.addAttribute("params", params);
    }
}