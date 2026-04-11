package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.ClassroomClient;
import com.lqm.admin_service.clients.FingerprintClient;
import com.lqm.admin_service.clients.UserClient;
import com.lqm.admin_service.dtos.ClassroomRequestDTO;
import com.lqm.admin_service.dtos.FingerprintRequestDTO;
import com.lqm.admin_service.dtos.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/classrooms/{id}/fingerprints")
public class ClassroomFingerprintController {

    private final ClassroomClient classroomClient;
    private final UserClient userClient;
    private final FingerprintClient fingerprintClient;
    private final com.lqm.admin_service.clients.DeviceClient deviceClient;

    @ModelAttribute("params")
    public Map<String, String> populateParams(@RequestParam Map<String, String> params) {
        return params;
    }

    @GetMapping
    public String enrollFingerprintView(@PathVariable UUID id, Model model) {
        ClassroomRequestDTO classroom = classroomClient.getClassroomRequestById(id);
        model.addAttribute("classroom", classroom);

        // Lấy danh sách học sinh của lớp
        if (classroom.studentIds() != null && !classroom.studentIds().isEmpty()) {
            List<UserResponseDTO> students = userClient.getUsersByIds(classroom.studentIds(), Map.of("role", "Student"))
                    .getContent();
            model.addAttribute("students", students);
        } else {
            model.addAttribute("students", Collections.emptyList());
        }

        // Kiểm tra xem lớp có thiết bị đang hoạt động không
        boolean hasActiveDevice = false;
        try {
            com.lqm.admin_service.dtos.DeviceResponseDTO device = deviceClient.getDeviceByClassroom(id);
            if (device != null && device.isActive()) {
                hasActiveDevice = true;
            }
        } catch (Exception e) {
            // Log hoặc bỏ qua (chưa có thiết bị)
        }
        model.addAttribute("hasActiveDevice", hasActiveDevice);

        return "attendance/fingerprint-enroll";
    }

    @PostMapping("/enroll")
    @ResponseBody
    public void enrollFingerprint(
            @PathVariable UUID id,
            @RequestParam UUID studentId,
            @RequestParam String studentName) {

        FingerprintRequestDTO request = FingerprintRequestDTO.builder()
                .classroomId(id)
                .studentId(studentId)
                .studentName(studentName)
                .build();

        fingerprintClient.enrollFingerprint(request);
    }

    @GetMapping("/cancel")
    public void cancelEnrollment(@PathVariable UUID id) {
        fingerprintClient.cancelEnrollment(id);
    }
}
