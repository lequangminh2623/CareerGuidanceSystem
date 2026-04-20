package com.lqm.score_service.consumers;

import com.lqm.score_service.configs.RabbitMQConfig;

import com.lqm.score_service.events.ScoreSyncEvent;
import com.lqm.score_service.services.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScoreEventConsumer {

    private final ScoreService scoreService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SCORE_SYNC)
    public void handleScoreSync(ScoreSyncEvent event) {
        log.info("Received ScoreSyncEvent: sectionIds={}, newStudents={}, removedStudents={}",
                event.sectionIds().size(), event.newStudentIds().size(), event.removedStudentIds().size());
        try {
            scoreService.syncScoresForClassroom(
                    event.sectionIds(),
                    event.newStudentIds(),
                    event.removedStudentIds());
            log.info("Successfully processed ScoreSyncEvent");
        } catch (Exception e) {
            log.error("Error processing ScoreSyncEvent: {}", e.getMessage(), e);
            throw e; // Re-throw to let RabbitMQ handle retry/DLQ
        }
    }
}
