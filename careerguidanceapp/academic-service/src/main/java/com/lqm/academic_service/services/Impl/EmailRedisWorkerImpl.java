package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.dtos.MailMessageDTO;
import com.lqm.academic_service.services.EmailRedisWorker;
import com.lqm.academic_service.utils.MailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailRedisWorkerImpl implements EmailRedisWorker {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MailUtils mailUtils;
    private static final String QUEUE_NAME = "mail-queue";

    @Override
    @Scheduled(fixedDelay = 5000) // Kiểm tra hàng đợi mỗi 5 giây
    public void consumeMessage() {
        try {
            Object messageObj = redisTemplate.opsForList().leftPop(QUEUE_NAME);
            if (messageObj instanceof MailMessageDTO(String v, String subject, String body)) {
                log.info("Processing mail message to: {}", v);
                mailUtils.sendEmail(v, subject, body);
            }
        } catch (Exception e) {
            log.error("Error consuming mail message from Redis", e);
        }
    }
}
