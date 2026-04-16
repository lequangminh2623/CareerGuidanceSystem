package com.lqm.chat_service.controller;

import com.lqm.chat_service.service.FirestoreGroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/admin/chats/groups")
@RequiredArgsConstructor
public class AdminGroupChatController {

    private final FirestoreGroupChatService groupChatService;

    public record CreateGroupRequest(UUID sectionId, String groupName, String teacherEmail, List<String> studentEmails) {}
    public record UpdateTeacherRequest(UUID sectionId, String oldTeacherEmail, String newTeacherEmail) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createGroup(@RequestBody CreateGroupRequest request) {
        groupChatService.createGroupChat(request.sectionId(), request.groupName(), request.teacherEmail(), request.studentEmails());
    }

    @PutMapping("/teacher")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTeacher(@RequestBody UpdateTeacherRequest request) {
        groupChatService.updateTeacher(request.sectionId(), request.oldTeacherEmail(), request.newTeacherEmail());
    }

    @DeleteMapping("/{sectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable UUID sectionId) {
        groupChatService.deleteGroupChat(sectionId);
    }

    @DeleteMapping("/batch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroupsBatch(@RequestParam List<UUID> sectionIds) {
        groupChatService.deleteGroupChatsBatch(sectionIds);
    }
}
