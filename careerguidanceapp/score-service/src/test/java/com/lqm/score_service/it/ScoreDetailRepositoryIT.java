package com.lqm.score_service.it;

import com.lqm.score_service.BaseIntegrationTest;
import com.lqm.score_service.models.ScoreDetail;
import com.lqm.score_service.repositories.ScoreDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ScoreDetailRepository — Integration Tests")
class ScoreDetailRepositoryIT extends BaseIntegrationTest {

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
        @DisplayName("findBySectionIdAndStudentId — Tìm chi tiết điểm theo sectionId và studentId")
        void findBySectionIdAndStudentId_Success() {
                ScoreDetail scoreDetail = ScoreDetail.builder()
                                .sectionId(sectionId)
                                .studentId(studentId)
                                .midtermScore(8.0)
                                .finalScore(9.0)
                                .build();
                scoreDetailRepository.save(scoreDetail);
                scoreDetailRepository.flush();

                Optional<ScoreDetail> found = scoreDetailRepository.findBySectionIdAndStudentId(sectionId, studentId);
                assertThat(found).isPresent();
                assertThat(found.get().getMidtermScore()).isEqualTo(8.0);
                assertThat(found.get().getFinalScore()).isEqualTo(9.0);
        }

        @Test
        @DisplayName("countIncompleteScores — Đếm số lượng cột điểm chưa hoàn thiện")
        void countIncompleteScores_ReturnsCorrectCount() {
                // 1. Điểm hoàn thiện (midterm & final đầy đủ)
                ScoreDetail completed = ScoreDetail.builder()
                                .sectionId(sectionId)
                                .studentId(UUID.randomUUID())
                                .midtermScore(8.0)
                                .finalScore(9.0)
                                .build();

                // 2. Điểm thiếu midterm
                ScoreDetail missingMidterm = ScoreDetail.builder()
                                .sectionId(sectionId)
                                .studentId(UUID.randomUUID())
                                .midtermScore(null)
                                .finalScore(8.5)
                                .build();

                // 3. Điểm thiếu final
                ScoreDetail missingFinal = ScoreDetail.builder()
                                .sectionId(sectionId)
                                .studentId(UUID.randomUUID())
                                .midtermScore(7.5)
                                .finalScore(null)
                                .build();

                scoreDetailRepository.saveAll(List.of(completed, missingMidterm, missingFinal));
                scoreDetailRepository.flush();

                int incompleteCount = scoreDetailRepository.countIncompleteScores(sectionId);
                assertThat(incompleteCount).isEqualTo(2);
        }

        @Test
        @DisplayName("deleteAllByStudentIdAndSectionIdIn — Xoá các cột điểm của học sinh theo danh sách lớp")
        void deleteAllByStudentIdAndSectionIdIn_Success() {
                UUID otherSection = UUID.randomUUID();
                ScoreDetail sd1 = ScoreDetail.builder()
                                .sectionId(sectionId)
                                .studentId(studentId)
                                .build();
                ScoreDetail sd2 = ScoreDetail.builder()
                                .sectionId(otherSection)
                                .studentId(studentId)
                                .build();
                scoreDetailRepository.saveAll(List.of(sd1, sd2));
                scoreDetailRepository.flush();

                scoreDetailRepository.deleteAllByStudentIdAndSectionIdIn(studentId, List.of(sectionId));
                scoreDetailRepository.flush();

                assertThat(scoreDetailRepository.findByStudentId(studentId)).hasSize(1);
                assertThat(scoreDetailRepository.findBySectionId(sectionId)).isEmpty();
        }

        @Test
        @DisplayName("deleteAllBySectionIdInAndStudentIdIn — Xoá hàng loạt điểm theo danh sách lớp và học sinh")
        void deleteAllBySectionIdInAndStudentIdIn_Success() {
                UUID student2 = UUID.randomUUID();
                ScoreDetail sd1 = ScoreDetail.builder()
                                .sectionId(sectionId)
                                .studentId(studentId)
                                .build();
                ScoreDetail sd2 = ScoreDetail.builder()
                                .sectionId(sectionId)
                                .studentId(student2)
                                .build();
                scoreDetailRepository.saveAll(List.of(sd1, sd2));
                scoreDetailRepository.flush();

                scoreDetailRepository.deleteAllBySectionIdInAndStudentIdIn(List.of(sectionId), List.of(studentId));
                scoreDetailRepository.flush();

                assertThat(scoreDetailRepository.findBySectionId(sectionId)).hasSize(1);
                assertThat(scoreDetailRepository.findBySectionId(sectionId).get(0).getStudentId()).isEqualTo(student2);
        }

        @Test
        @DisplayName("findAll with Specification — Lấy tất cả có filter và phân trang")
        void findAllWithSpecification_Success() {
                ScoreDetail scoreDetail = ScoreDetail.builder()
                                .sectionId(sectionId)
                                .studentId(studentId)
                                .midtermScore(9.5)
                                .build();
                scoreDetailRepository.save(scoreDetail);
                scoreDetailRepository.flush();

                Specification<ScoreDetail> spec = (root, query, cb) -> cb.equal(root.get("sectionId"), sectionId);
                Page<ScoreDetail> page = scoreDetailRepository.findAll(spec, PageRequest.of(0, 10));

                assertThat(page.getContent()).hasSize(1);
                assertThat(page.getContent().get(0).getMidtermScore()).isEqualTo(9.5);
        }
}
