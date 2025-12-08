package com.lqm.academic_service.controllers;

import com.lqm.academic_service.clients.ClassroomClient;
import com.lqm.academic_service.clients.GradeClient;
import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.*;
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

import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/classrooms")
public class ClassroomController {

    private final ClassroomClient classroomClient;
    private final UserClient userClient;
    private final GradeClient gradeClient;
    private final PageableUtil pageableUtil;
    private final MessageSource messageSource;
    private final WebAppValidator webAppValidator;

    @InitBinder({"classroom"})
    public void initFormBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @ModelAttribute("students")
    public List<UserResponseDTO> students() {
        return userClient.getUsers(List.of(), Map.of("role", "STUDENT"))
                .getContent();
    }

    //lấy grades theo year
    @ModelAttribute("grades")
    public List<GradeDetailsResponseDTO> grades() {
        return gradeClient.getGradesDetails(Map.of());
    }

    @GetMapping("")
    public String listClassrooms(Model model,
                                 @RequestParam Map<String, String> params) {

        Pageable pageable = pageableUtil.getPageable(
                params.get("page"),
                PageSize.CLASSROOM_PAGE_SIZE,
                List.of()
        );

        Page<ClassroomResponseDTO> classroomResp =
                classroomClient.getClassrooms(params, pageable);

        model.addAttribute("classrooms", classroomResp.getContent());
        model.addAttribute("totalPages", classroomResp.getTotalPages());
        model.addAttribute("currentPage", params.getOrDefault("page", "1"));

        return "classroom/classroom-list";
    }

    @GetMapping("/add")
    public String addClassroom(Model model) {
        model.addAttribute("classroom", ClassroomRequestDTO.builder().build());
        return "classroom/classroom-form";
    }

    @GetMapping("/{id}")
    public String updateClassroom(@PathVariable UUID id, Model model) {
        model.addAttribute("classroom", classroomClient.getClassroomRequestById(id));
        return "classroom/classroom-form";
    }

    @PostMapping("")
    public String saveClassroom(
            @Valid @ModelAttribute("classroom") ClassroomRequestDTO dto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage",
                    messageSource.getMessage("error", null, Locale.getDefault()));
            return "classroom/classroom-form";
        }

        classroomClient.saveClassroom(dto);
        return "redirect:/classrooms";
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClassroom(@PathVariable UUID id) {
        classroomClient.deleteClassroom(id);
    }

    @DeleteMapping("/{classroomId}/students/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeStudent(
            @PathVariable UUID classroomId,
            @PathVariable UUID studentId) {

        classroomClient.removeStudent(classroomId, studentId);
    }
}
