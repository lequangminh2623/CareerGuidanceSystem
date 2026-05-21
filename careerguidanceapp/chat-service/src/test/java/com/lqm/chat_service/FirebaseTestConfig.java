package com.lqm.chat_service;

import com.lqm.chat_service.config.FirebaseConfig;
import com.lqm.chat_service.service.FirebaseTokenService;
import com.lqm.chat_service.service.FirestoreGroupChatService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Mock Firebase services để ngăn FirebaseConfig cố gắng kết nối
 * Firebase thực trong quá trình Integration Test.
 * Được load thông qua @Import trong BaseIntegrationTest.
 */
@TestConfiguration
public class FirebaseTestConfig {

    @Bean
    @Primary
    public FirebaseConfig firebaseConfig() {
        return Mockito.mock(FirebaseConfig.class);
    }

    @Bean
    @Primary
    public FirestoreGroupChatService firestoreGroupChatService() {
        return Mockito.mock(FirestoreGroupChatService.class);
    }

    @Bean
    @Primary
    public FirebaseTokenService firebaseTokenService() {
        return Mockito.mock(FirebaseTokenService.class);
    }
}
