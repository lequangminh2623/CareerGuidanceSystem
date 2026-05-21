package com.lqm.chat_service.service.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "unchecked"})
class FirestoreGroupChatServiceImplTest {

    @InjectMocks
    private FirestoreGroupChatServiceImpl groupChatService;

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private ApiFuture<WriteResult> writeResultFuture;

    @Mock
    private ApiFuture<DocumentSnapshot> documentSnapshotFuture;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private WriteBatch writeBatch;

    @BeforeEach
    void setUp() {
        lenient().when(firestore.collection(anyString())).thenReturn(collectionReference);
        lenient().when(collectionReference.document(anyString())).thenReturn(documentReference);
        lenient().when(firestore.batch()).thenReturn(writeBatch);
    }

    @Test
    @DisplayName("createGroupChat: success")
    void createGroupChat_Success() throws Exception {
        UUID sectionId = UUID.randomUUID();
        when(documentReference.set(anyMap())).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenReturn(null);

        try (MockedStatic<FirestoreClient> mockedStatic = mockStatic(FirestoreClient.class)) {
            mockedStatic.when(FirestoreClient::getFirestore).thenReturn(firestore);

            groupChatService.createGroupChat(sectionId, "Group1", "teacher@test.com", List.of("student@test.com"));

            verify(documentReference).set(anyMap());
        }
    }

    @Test
    @DisplayName("createGroupChat: throws exception on null ID")
    void createGroupChat_NullSectionId_ThrowsException() {
        try (MockedStatic<FirestoreClient> mockedStatic = mockStatic(FirestoreClient.class)) {
            mockedStatic.when(FirestoreClient::getFirestore).thenReturn(firestore);
            assertThatThrownBy(() -> groupChatService.createGroupChat(null, "Group1", "t@test.com", List.of()))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    @DisplayName("createGroupChat: execution exception translates to runtime")
    void createGroupChat_ExecutionException() throws Exception {
        UUID sectionId = UUID.randomUUID();
        when(documentReference.set(anyMap())).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenThrow(new InterruptedException("Interrupted"));

        try (MockedStatic<FirestoreClient> mockedStatic = mockStatic(FirestoreClient.class)) {
            mockedStatic.when(FirestoreClient::getFirestore).thenReturn(firestore);

            assertThatThrownBy(() -> groupChatService.createGroupChat(sectionId, "Group1", "teacher@test.com", List.of("student@test.com")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to create group chat in Firestore");
        }
    }

    @Test
    @DisplayName("updateTeacher: success when document exists")
    void updateTeacher_Success() throws Exception {
        UUID sectionId = UUID.randomUUID();
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        
        when(documentReference.update(anyMap())).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenReturn(null);

        try (MockedStatic<FirestoreClient> mockedStatic = mockStatic(FirestoreClient.class)) {
            mockedStatic.when(FirestoreClient::getFirestore).thenReturn(firestore);

            groupChatService.updateTeacher(sectionId, "old@test.com", "new@test.com");

            verify(documentReference, times(2)).update(anyMap());
        }
    }

    @Test
    @DisplayName("updateTeacher: returns early when document does not exist")
    void updateTeacher_DocumentNotFound_ReturnsEarly() throws Exception {
        UUID sectionId = UUID.randomUUID();
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        try (MockedStatic<FirestoreClient> mockedStatic = mockStatic(FirestoreClient.class)) {
            mockedStatic.when(FirestoreClient::getFirestore).thenReturn(firestore);

            groupChatService.updateTeacher(sectionId, "old@test.com", "new@test.com");

            verify(documentReference, never()).update(anyMap());
        }
    }

    @Test
    @DisplayName("deleteGroupChat: success")
    void deleteGroupChat_Success() throws Exception {
        UUID sectionId = UUID.randomUUID();
        when(documentReference.delete()).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenReturn(null);

        try (MockedStatic<FirestoreClient> mockedStatic = mockStatic(FirestoreClient.class)) {
            mockedStatic.when(FirestoreClient::getFirestore).thenReturn(firestore);

            groupChatService.deleteGroupChat(sectionId);

            verify(documentReference).delete();
        }
    }

    @Test
    @DisplayName("deleteGroupChatsBatch: success")
    void deleteGroupChatsBatch_Success() throws Exception {
        UUID sectionId1 = UUID.randomUUID();
        UUID sectionId2 = UUID.randomUUID();
        List<UUID> sectionIds = List.of(sectionId1, sectionId2);
        
        ApiFuture<List<WriteResult>> listApiFuture = mock(ApiFuture.class);
        when(writeBatch.commit()).thenReturn(listApiFuture);
        when(listApiFuture.get()).thenReturn(null);

        try (MockedStatic<FirestoreClient> mockedStatic = mockStatic(FirestoreClient.class)) {
            mockedStatic.when(FirestoreClient::getFirestore).thenReturn(firestore);

            groupChatService.deleteGroupChatsBatch(sectionIds);

            verify(writeBatch, times(2)).delete(any(DocumentReference.class));
            verify(writeBatch).commit();
        }
    }

    @Test
    @DisplayName("deleteGroupChatsBatch: returns early when list is empty")
    void deleteGroupChatsBatch_EmptyList_ReturnsEarly() {
        try (MockedStatic<FirestoreClient> mockedStatic = mockStatic(FirestoreClient.class)) {
            groupChatService.deleteGroupChatsBatch(List.of());

            mockedStatic.verify(FirestoreClient::getFirestore, never());
        }
    }
}
