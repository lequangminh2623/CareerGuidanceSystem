package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.AttendanceConfigClient;
import com.lqm.admin_service.clients.DeviceClient;
import com.lqm.admin_service.dtos.AttendanceConfigDTO;
import com.lqm.admin_service.dtos.DeviceResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/devices")
public class DeviceController {
    private final DeviceClient deviceClient;
    private final AttendanceConfigClient attendanceConfigClient;

    // Hiển thị danh sách devices
    @GetMapping
    public String listDevices(Model model, @RequestParam Map<String, String> params) {
        model.addAttribute("params", params); // luôn set trước để template không bị NPE
        try {
            Page<DeviceResponseDTO> devices = deviceClient.getDevices(params);
            model.addAttribute("devices", devices);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        // Load cấu hình điểm danh
        try {
            AttendanceConfigDTO config = attendanceConfigClient.getConfig();
            if (config != null) {
                model.addAttribute("attendanceConfig", config);
            } else {
                throw new RuntimeException("Config is null");
            }
        } catch (Exception e) {
            // Fallback default nếu không load được
            model.addAttribute("attendanceConfig", AttendanceConfigDTO.builder()
                    .sessionsPerDay(1)
                    .morningStartTime(LocalTime.of(7, 0))
                    .morningEndTime(LocalTime.of(11, 30))
                    .afternoonStartTime(LocalTime.of(13, 0))
                    .afternoonEndTime(LocalTime.of(17, 0))
                    .build());
            model.addAttribute("configWarning", "Không thể tải cấu hình điểm danh từ server.");
        }

        return "device/list";
    }

    // Toggle active status
    @PatchMapping("/{id}")
    public String toggleDeviceActive(
            @PathVariable String id,
            @RequestParam boolean active,
            RedirectAttributes redirectAttributes) {
        try {
            deviceClient.updateDeviceActiveStatus(id, active);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/devices";
    }

    // Xóa device
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteDevice(@PathVariable String id) {
        deviceClient.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    // Lưu cấu hình điểm danh
    @PostMapping("/attendance-config")
    public String saveAttendanceConfig(
            @RequestParam int sessionsPerDay,
            @RequestParam String morningStartTime,
            @RequestParam String morningEndTime,
            @RequestParam String afternoonStartTime,
            @RequestParam String afternoonEndTime,
            RedirectAttributes redirectAttributes) {
        try {
            AttendanceConfigDTO dto = AttendanceConfigDTO.builder()
                    .sessionsPerDay(sessionsPerDay)
                    .morningStartTime(LocalTime.parse(morningStartTime))
                    .morningEndTime(LocalTime.parse(morningEndTime))
                    .afternoonStartTime(LocalTime.parse(afternoonStartTime))
                    .afternoonEndTime(LocalTime.parse(afternoonEndTime))
                    .build();
            attendanceConfigClient.updateConfig(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu cấu hình điểm danh thành công!");
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("\"message\"")) {
                // Trích xuất message từ response
                int start = msg.indexOf("\"message\":\"") + 11;
                int end = msg.indexOf("\"", start);
                if (start > 10 && end > start) {
                    msg = msg.substring(start, end);
                }
            }
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cấu hình: " + msg);
        }
        return "redirect:/devices";
    }
}
