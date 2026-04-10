package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.dtos.MailMessageDTO;
import com.lqm.academic_service.services.EmailRedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailRedisPublisherImpl implements EmailRedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String QUEUE_NAME = "mail-queue";

    @Override
    public void publish(MailMessageDTO message) {
        redisTemplate.opsForList().rightPush(QUEUE_NAME, message);
    }
}
