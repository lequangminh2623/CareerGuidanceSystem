package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.DeviceClient;
import com.lqm.admin_service.dtos.DeviceRequestDTO;
import com.lqm.admin_service.dtos.DeviceResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/classrooms/{classroomId}/devices")
public class ClassroomDeviceController {
    private final DeviceClient deviceClient;

    @ModelAttribute("params")
    public Map<String, String> populateParams(
            @RequestParam Map<String, String> params,
            @PathVariable(required = false) UUID classroomId) {

        if (classroomId != null) {
            params.put("classroomId", classroomId.toString());
        }

        return params;
    }

    // Hiển thị trang gán device cho lớp
    @GetMapping
    public String showAssignDevicePage(@PathVariable UUID classroomId, Model model) {
        List<DeviceResponseDTO> availableDevices = java.util.Collections.emptyList();
        DeviceResponseDTO assignedDevice = null;

        try {
            availableDevices = deviceClient.getAvailableDevices();
            if (availableDevices == null) {
                availableDevices = java.util.Collections.emptyList();
            }

            try {
                assignedDevice = deviceClient.getDeviceByClassroom(classroomId);
            } catch (Exception e) {
                // Lớp chưa có device gán
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Không thể lấy danh sách thiết bị: " + e.getMessage());
        }

        model.addAttribute("availableDevices", availableDevices);
        model.addAttribute("assignedDevice", assignedDevice);
        model.addAttribute("classroomId", classroomId);

        return "device/form";
    }

    // Gán device vào lớp
    @PostMapping("/{deviceId}/assign")
    public String assignDeviceToClassroom(@PathVariable UUID classroomId,
            @PathVariable String deviceId,
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes) {
        try {
            deviceClient.assignDeviceToClassroom(
                    DeviceRequestDTO.builder().id(deviceId).classroomId(classroomId).build());
            redirectAttributes.addFlashAttribute("successMessage", "Thiết bị đã được gán vào lớp!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        String redirectUrl = "redirect:/classrooms/" + classroomId + "/devices";
        if (params.get("gradeId") != null && !params.get("gradeId").isEmpty()) {
            redirectUrl += "?gradeId=" + params.get("gradeId");
        }
        return redirectUrl;
    }

    // Bỏ gán device từ lớp
    @PostMapping("/{deviceId}/unassign")
    public String unassignDeviceFromClassroom(@PathVariable UUID classroomId,
            @PathVariable String deviceId,
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes) {
        try {
            deviceClient.unassignDeviceFromClassroom(deviceId);
            redirectAttributes.addFlashAttribute("successMessage", "Thiết bị đã được bỏ gán!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        String redirectUrl = "redirect:/classrooms/" + classroomId + "/devices";
        if (params.get("gradeId") != null && !params.get("gradeId").isEmpty()) {
            redirectUrl += "?gradeId=" + params.get("gradeId");
        }
        return redirectUrl;
    }
}
