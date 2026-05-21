package com.lqm.chat_service.service.impl;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import com.google.firebase.cloud.FirestoreClient;
import com.lqm.chat_service.service.FirestoreGroupChatService;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FirestoreGroupChatServiceImpl implements FirestoreGroupChatService {

    private static final String GROUPS_COLLECTION = "groups";

    private String sanitizeUid(String email) {
        return email != null ? email : "";
    }

    @Override
    public void createGroupChat(@Nonnull UUID sectionId, String groupName, String teacherEmail,
            List<String> studentEmails) {
        Firestore db = FirestoreClient.getFirestore();
        String sectionIdStr = sectionId.toString();
        if (sectionIdStr == null) {
            throw new IllegalArgumentException("Section ID cannot be null");
        }
        DocumentReference docRef = db.collection(GROUPS_COLLECTION)
                .document(sectionIdStr);

        String adminId = sanitizeUid(teacherEmail);
        List<String> rawMembers = new ArrayList<>(studentEmails);
        rawMembers.add(teacherEmail);

        List<String> members = rawMembers.stream()
                .map(this::sanitizeUid)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("groupName", groupName);
        data.put("adminId", adminId);
        data.put("members", members);
        data.put("lastMessage", null);
        data.put("lastMessageAt", null);
        data.put("lastSenderId", null);
        data.put("seenBy", new ArrayList<>());
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("type", "group");

        try {
            docRef.set(data).get();
            log.info("Created group chat in Firestore for section: {}", sectionId);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to create group chat for section: {}", sectionId, e);
            throw new RuntimeException("Failed to create group chat in Firestore", e);
        }
    }

    @Override
    public void updateTeacher(@Nonnull UUID sectionId, String oldTeacherEmail, String newTeacherEmail) {
        Firestore db = FirestoreClient.getFirestore();
        String sectionIdStr = sectionId.toString();
        if (sectionIdStr == null) {
            throw new IllegalArgumentException("Section ID cannot be null");
        }
        DocumentReference docRef = db.collection(GROUPS_COLLECTION)
                .document(sectionIdStr);

        String oldAdmin = sanitizeUid(oldTeacherEmail);
        String newAdmin = sanitizeUid(newTeacherEmail);

        Map<String, Object> updates = new HashMap<>();
        updates.put("adminId", newAdmin);
        updates.put("members", FieldValue.arrayRemove(oldAdmin));

        try {
            if (!docRef.get().get().exists()) {
                log.warn("Group chat document not found for section: {}. Skipping teacher update.", sectionId);
                return;
            }

            docRef.update(updates).get();
            // Then add new member (requires two updates since FieldValue doesn't easily
            // chain remove/union cleanly in this Map format predictably)
            Map<String, Object> addMember = new HashMap<>();
            addMember.put("members", FieldValue.arrayUnion(newAdmin));
            docRef.update(addMember).get();
            log.info("Updated teacher for group chat in Firestore for section: {}", sectionId);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to update teacher in group chat for section: {}", sectionId, e);
            throw new RuntimeException("Failed to update teacher in Firestore", e);
        }
    }

    @Override
    public void deleteGroupChat(@Nonnull UUID sectionId) {
        Firestore db = FirestoreClient.getFirestore();
        String sectionIdStr = sectionId.toString();
        if (sectionIdStr == null) {
            throw new IllegalArgumentException("Section ID cannot be null");
        }
        DocumentReference docRef = db.collection(GROUPS_COLLECTION)
                .document(sectionIdStr);

        try {
            docRef.delete().get();
            log.info("Deleted group chat in Firestore for section: {}", sectionId);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to delete group chat for section: {}", sectionId, e);
            throw new RuntimeException("Failed to delete group chat in Firestore", e);
        }
    }

    @Override
    public void deleteGroupChatsBatch(List<UUID> sectionIds) {
        if (sectionIds == null || sectionIds.isEmpty())
            return;

        Firestore db = FirestoreClient.getFirestore();
        WriteBatch batch = db.batch();

        for (UUID sectionId : sectionIds) {
            if (sectionId == null)
                continue;
            String sectionIdStr = sectionId.toString();
            if (sectionIdStr == null) {
                throw new IllegalArgumentException("Section ID cannot be null");
            }
            DocumentReference docRef = db.collection(GROUPS_COLLECTION)
                    .document(sectionIdStr);
            batch.delete(docRef);
        }

        try {
            batch.commit().get();
            log.info("Deleted {} group chats in Firestore via batch", sectionIds.size());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to batch delete group chats", e);
            throw new RuntimeException("Failed to batch delete group chats in Firestore", e);
        }
    }
}
