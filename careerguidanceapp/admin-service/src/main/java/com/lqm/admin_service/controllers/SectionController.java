package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.*;
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
@RequestMapping("/classrooms/{classroomId}/sections")
public class SectionController {

    private final SectionClient sectionClient;
    private final MessageSource messageSource;
    private final UserClient userClient;
    private final ClassroomClient classroomClient;
    private final CurriculumClient curriculumClient;
    private final GradeClient gradeClient;

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

    @GetMapping("/teachers/search")
    @ResponseBody
    public Map<String, Object> searchTeachers(
            @RequestParam(value = "term") String term,
            @RequestParam(value = "page", defaultValue = "0") String page) {

        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("role", "Teacher");
        searchParams.put("page", page);

        if (!term.isBlank()) {
            searchParams.put("kw", term);
        }

        Page<UserResponseDTO> userPage = userClient.getUsers(List.of(), searchParams);

        List<Map<String, Object>> results = userPage.getContent().stream().map(u -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", u.id().toString());
            item.put("text", u.lastName() + " " + u.firstName());
            return item;
        }).toList();

        return Map.of(
                "results", results,
                "hasMore", !userPage.isLast()
        );
    }

    @GetMapping
    public String listSections(Model model, @RequestParam Map<String, String> params, @PathVariable String classroomId) {
        populateModel(model, params, classroomId);
        return "section/list";
    }

    @PostMapping
    public String saveSection(@PathVariable String classroomId,
                              @ModelAttribute("sections") @Valid SectionListRequest request,
                              BindingResult bindingResult,
                              Model model,
                              @RequestParam Map<String, String> params) {

        if (bindingResult.hasErrors()) {
            populateModel(model, params, classroomId);
            return "section/list";
        }

        try {
            sectionClient.saveSections(request, Map.of("classroomId", classroomId));
            return "redirect:/classrooms?gradeId=" + params.get("gradeId");

        } catch (ValidationException e) {
            if (e.getDetails() instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, String> errors = (Map<String, String>) e.getDetails();
                errors.forEach((field, message) -> {
                    bindingResult.rejectValue(field, "error.section", message);
                });
            }
            populateModel(model, params, classroomId);
            return "section/list";

        } catch (Exception e) {
            model.addAttribute("errorMessage", messageSource.getMessage("error", null, Locale.getDefault()));
            populateModel(model, params, classroomId);
            return "section/list";
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSection(@PathVariable UUID id) {
        sectionClient.deleteSection(id);
    }

    private void populateModel(Model model, Map<String, String> params, String classroomId) {
        UUID classId = UUID.fromString(classroomId);

        ClassroomRequestDTO classroom = classroomClient.getClassroomRequestById(classId);
        model.addAttribute("classroomName", classroom.name());

        params.put("classroomId", classroomId);
        params.put("page", "");
        Page<SectionRequestDTO> sectionDTOPage = sectionClient.getSectionRequests(params);
        model.addAttribute("sections", sectionDTOPage);
        model.addAttribute("params", params);

        Map<String, String> curriculumParams = new HashMap<>();
        curriculumParams.put("gradeId", classroom.gradeId().toString());
        curriculumParams.put("page", "");
        List<CurriculumResponseDTO> curriculumList = curriculumClient.getCurriculums(curriculumParams).getContent();

        Map<UUID, CurriculumResponseDTO> curriculumsMap = curriculumList.stream()
                .collect(Collectors.toMap(CurriculumResponseDTO::id, c -> c));
        model.addAttribute("curriculums", curriculumsMap);

        Set<UUID> teacherIds = sectionDTOPage.getContent().stream()
                .map(SectionRequestDTO::teacherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!teacherIds.isEmpty()) {
            List<UserResponseDTO> teacherList = userClient.getUsers(new ArrayList<>(teacherIds), Map.of()).getContent();
            Map<UUID, UserResponseDTO> teachersMap = teacherList.stream()
                    .collect(Collectors.toMap(UserResponseDTO::id, t -> t));
            model.addAttribute("teachers", teachersMap);
        } else {
            model.addAttribute("teachers", Collections.emptyMap());
        }
    }
}