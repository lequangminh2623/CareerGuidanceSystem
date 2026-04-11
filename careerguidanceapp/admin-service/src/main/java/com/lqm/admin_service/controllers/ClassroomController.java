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
@RequestMapping("/classrooms")
public class ClassroomController {

    private final ClassroomClient classroomClient;
    private final UserClient userClient;
    private final GradeClient gradeClient;
    private final MessageSource messageSource;

    @ModelAttribute("params")
    public Map<String, String> populateParams(@RequestParam Map<String, String> params) {
        return params;
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
                                })));
    }

    // Nạp danh sách các sinh viên ĐÃ CÓ trong lớp để hiển thị ở cột phải (Dual
    // Listbox)
    @ModelAttribute("selectedStudents")
    public List<UserResponseDTO> populateSelectedStudents(@PathVariable(required = false) UUID id) {
        if (id == null) {
            return Collections.emptyList();
        }

        try {
            ClassroomRequestDTO classroom = classroomClient.getClassroomRequestById(id);
            if (classroom.studentIds() != null && !classroom.studentIds().isEmpty()) {
                // Gọi API lấy danh sách User theo danh sách ID
                return userClient.getUsersByIds(classroom.studentIds(), Map.of("role", "Student")).getContent();
            }
        } catch (Exception e) {
            // Handle exception if needed
        }
        return Collections.emptyList();
    }

    @GetMapping
    public String listClassrooms(Model model, @RequestParam Map<String, String> params) {
        // Chỉ fetch list nếu đã chọn Khối lớp
        if (params.containsKey("gradeId") && !params.get("gradeId").isEmpty()) {
            Page<ClassroomResponseDTO> classroomDTOPage = classroomClient.getClassrooms(params);
            model.addAttribute("classrooms", classroomDTOPage);
        } else {
            model.addAttribute("classrooms", Page.empty());
        }

        return "classroom/list";
    }

    @GetMapping("/add")
    public String addClassroom(Model model, @RequestParam(required = false) UUID gradeId) {
        model.addAttribute("classroom", ClassroomRequestDTO.builder().gradeId(gradeId).build());

        return "classroom/form";
    }

    @GetMapping("/{id}")
    public String updateClassroom(@PathVariable UUID id, Model model) {
        model.addAttribute("classroom", classroomClient.getClassroomRequestById(id));
        return "classroom/form";
    }

    @PostMapping("")
    public String saveClassroom(@ModelAttribute("classroom") @Valid ClassroomRequestDTO classroomRequestDTO,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "classroom/form";
        }

        try {
            classroomClient.saveClassroom(classroomRequestDTO);
            return "redirect:/classrooms?gradeId=" + classroomRequestDTO.gradeId();

        } catch (ValidationException e) {
            if (e.getDetails() instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, String> errors = (Map<String, String>) e.getDetails();

                errors.forEach((field, message) -> {
                    bindingResult.rejectValue(field, "error.classroom", message);
                });
            }

            return "classroom/form";

        } catch (Exception e) {
            model.addAttribute("errorMessage", messageSource.getMessage("error", null, Locale.getDefault()));

            return "classroom/form";
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClassroom(@PathVariable UUID id) {
        classroomClient.deleteClassroom(id);
    }

    @GetMapping("/students/search")
    @ResponseBody
    public Map<String, Object> searchStudents(
            @RequestParam(value = "term") String term,
            @RequestParam(value = "page") String page) {

        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("role", "Student");
        searchParams.put("page", page);

        if (!term.isBlank()) {
            searchParams.put("kw", term);
        }

        Page<UserResponseDTO> userPage = userClient.getUsers(searchParams);

        List<Map<String, Object>> results = userPage.getContent().stream().map(u -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", u.id().toString());
            item.put("text", u.code() + " - " + u.lastName() + " " + u.firstName());
            return item;
        }).toList();

        return Map.of(
                "results", results,
                "hasMore", !userPage.isLast());
    }
}