package com.lqm.score_service.it;

import com.lqm.score_service.BaseIntegrationTest;
import com.lqm.score_service.clients.ClassroomClient;
import com.lqm.score_service.clients.SectionClient;
import com.lqm.score_service.clients.UserClient;
import com.lqm.score_service.configs.RabbitMQConfig;
import com.lqm.score_service.events.ScoreSyncEvent;
import com.lqm.score_service.models.ScoreDetail;
import com.lqm.score_service.repositories.ScoreDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ScoreEventConsumer — Integration Tests")
class ScoreEventConsumerIT extends BaseIntegrationTest {

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private ClassroomClient classroomClient;

    @MockitoBean
    private SectionClient sectionClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ScoreDetailRepository scoreDetailRepository;

    private UUID sectionId;
    private UUID studentId;

    @BeforeEach
    void setUp() {
        scoreDetailRepository.deleteAll();
        scoreDetailRepository.flush();

        sectionId = UUID.randomUUID();
        studentId = UUID.randomUUID();
    }

    @Test
    @DisplayName("ScoreSyncEvent — Đồng bộ thêm học sinh mới và xoá học sinh khỏi lớp học phần")
    void handleScoreSync_AddAndRemoveStudents_Success() throws Exception {
        // 1. Gửi sự kiện thêm học sinh mới
        ScoreSyncEvent addEvent = new ScoreSyncEvent(
                List.of(sectionId),
                List.of(studentId),
                List.of()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE,
                RabbitMQConfig.RK_SCORE_SYNC,
                addEvent
        );

        // Chờ xử lý bất đồng bộ
        boolean added = false;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(200);
            List<ScoreDetail> details = scoreDetailRepository.findBySectionId(sectionId);
            if (!details.isEmpty()) {
                added = true;
                break;
            }
        }

        assertThat(added).isTrue();
        List<ScoreDetail> detailsAfterAdd = scoreDetailRepository.findBySectionId(sectionId);
        assertThat(detailsAfterAdd).hasSize(1);
        assertThat(detailsAfterAdd.get(0).getStudentId()).isEqualTo(studentId);

        // 2. Gửi sự kiện xoá học sinh khỏi lớp
        ScoreSyncEvent removeEvent = new ScoreSyncEvent(
                List.of(sectionId),
                List.of(),
                List.of(studentId)
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE,
                RabbitMQConfig.RK_SCORE_SYNC,
                removeEvent
        );

        // Chờ xử lý bất đồng bộ
        boolean removed = false;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(200);
            List<ScoreDetail> details = scoreDetailRepository.findBySectionId(sectionId);
            if (details.isEmpty()) {
                removed = true;
                break;
            }
        }

        assertThat(removed).isTrue();
        assertThat(scoreDetailRepository.findBySectionId(sectionId)).isEmpty();
    }
}
