package com.lqm.chat_service.service;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public interface FirestoreGroupChatService {

    void createGroupChat(@Nonnull UUID sectionId, String groupName, String teacherEmail, List<String> studentEmails);

    void updateTeacher(@Nonnull UUID sectionId, String oldTeacherEmail, String newTeacherEmail);

    void deleteGroupChat(@Nonnull UUID sectionId);

    void deleteGroupChatsBatch(List<UUID> sectionIds);
}
