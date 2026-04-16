package com.lqm.academic_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/chat-service/api/internal/admin/chats/groups", contextId = "chatAdminClient")
public interface ChatAdminClient {

    record CreateGroupRequest(UUID sectionId, String groupName, String teacherEmail, List<String> studentEmails) {}
    record UpdateTeacherRequest(UUID sectionId, String oldTeacherEmail, String newTeacherEmail) {}

    @PostMapping
    void createGroupChat(@RequestBody CreateGroupRequest request);

    @PutMapping("/teacher")
    void updateTeacher(@RequestBody UpdateTeacherRequest request);

    @DeleteMapping("/{sectionId}")
    void deleteGroupChat(@PathVariable("sectionId") UUID sectionId);

    @DeleteMapping("/batch")
    void deleteGroupChatsBatch(@RequestParam("sectionIds") List<UUID> sectionIds);
}
