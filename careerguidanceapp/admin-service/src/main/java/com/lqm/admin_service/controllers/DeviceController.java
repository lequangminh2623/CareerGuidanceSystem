package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.DeviceClient;
import com.lqm.admin_service.dtos.DeviceResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/devices")
public class DeviceController {
    private final DeviceClient deviceClient;

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
}
