package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.AttendanceClient;
import com.lqm.admin_service.clients.ClassroomClient;
import com.lqm.admin_service.dtos.*;
import com.lqm.admin_service.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceClient attendanceClient;
    private final ClassroomClient classroomClient;
    private final MessageSource messageSource;

    @GetMapping
    public String listAttendances(Model model,
                                  @RequestParam Map<String, String> params) {
        populateModel(model, params);

        return "attendance/form";
    }

    @PostMapping
    public String saveAttendances(@ModelAttribute AttendanceListRequest request,
                                  BindingResult bindingResult,
                                  Model model,
                                  @RequestParam Map<String, String> params) {
        if (bindingResult.hasErrors()) {
            populateModel(model, params);
            return "attendance/form";
        }

        try {
            LocalDate date = LocalDate.parse(params.get("attendanceDate"));
            UUID classroomId = UUID.fromString(params.get("classroomId"));
            attendanceClient.saveAttendances(classroomId, date, request);
            return "redirect:/classrooms?gradeId=" + params.getOrDefault("gradeId", "");
        } catch (ValidationException e) {
            if (e.getDetails() instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, String> errors = (Map<String, String>) e.getDetails();
                errors.forEach((field, message) -> {
                    bindingResult.rejectValue(field, "error.attendance", message);
                });
            }
            populateModel(model, params);
            return "attendance/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", messageSource.getMessage("error", null, Locale.getDefault()));
            populateModel(model, params);
            return "attendance/form";
        }
    }

    @PostMapping("/delete")
    public String deleteAttendances(@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
        try {
            UUID classroomId = UUID.fromString(params.get("classroomId"));
            LocalDate date = LocalDate.parse(params.get("attendanceDate"));
            attendanceClient.deleteAttendances(classroomId, date);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa toàn bộ điểm danh của ngày " + params.get("attendanceDate") + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa: " + e.getMessage());
        }

        String redirectUrl = "redirect:/attendances";
        if (params.get("classroomId") != null && params.get("attendanceDate") != null) {
            redirectUrl += "?classroomId=" + params.get("classroomId") + "&attendanceDate=" + params.get("attendanceDate");
        }
        if (params.get("gradeId") != null && !params.get("gradeId").isEmpty()) {
            redirectUrl += "&gradeId=" + params.get("gradeId");
        }
        return redirectUrl;
    }

    private void populateModel(Model model, Map<String, String> params) {
        model.addAttribute("params", params);

        if (params.get("classroomId") != null && params.get("attendanceDate") != null && !params.get("attendanceDate").isEmpty()) {
            UUID classroomId = UUID.fromString(params.get("classroomId"));
            LocalDate date = LocalDate.parse(params.get("attendanceDate") );
            List<AdminAttendanceResponseDTO> attendances = attendanceClient.getAttendances(classroomId, date);
            List<UserResponseDTO> students = classroomClient.getStudentsInClassroom(classroomId, Map.of()).getContent();

            Map<UUID, UserResponseDTO> studentMap = students.stream()
                    .collect(Collectors.toMap(UserResponseDTO::id, Function.identity()));

            ClassroomResponseDTO classroom = classroomClient.getClassroomResponseById(classroomId);

            model.addAttribute("attendances", attendances);
            model.addAttribute("students", studentMap);
            model.addAttribute("classroom", classroom);
        }
    }
}
