package com.lqm.chat_service;

import org.junit.jupiter.api.Test;

/**
 * Context load test — đảm bảo Spring ApplicationContext khởi động thành công
 * với RabbitMQ Testcontainer và Firebase mocked.
 */
class ChatServiceApplicationTests extends BaseIntegrationTest {

    @Test
    void contextLoads() {
        // Nếu context load không thành công, test này sẽ fail
    }
}
