package com.lqm.score_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "score_details")
@NamedQueries({
        @NamedQuery(name = "ScoreDetail.findAll", query = "SELECT s FROM ScoreDetail s"),
        @NamedQuery(name = "ScoreDetail.findById", query = "SELECT s FROM ScoreDetail s WHERE s.id = :id"),
        @NamedQuery(name = "ScoreDetail.findByFinalScore", query = "SELECT s FROM ScoreDetail s WHERE s.finalScore = :finalScore"),
        @NamedQuery(name = "ScoreDetail.findByMidtermScore", query = "SELECT s FROM ScoreDetail s WHERE s.midtermScore = :midtermScore"),
        @NamedQuery(name = "ScoreDetail.findByUpdatedDate", query = "SELECT s FROM ScoreDetail s WHERE s.updatedDate = :updatedDate")
})
public class ScoreDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Max(10)
    @Min(0)
    @Column(name = "final_score")
    @ToString.Include
    private Double finalScore;

    @Max(10)
    @Min(0)
    @Column(name = "midterm_score")
    @ToString.Include
    private Double midtermScore;

    @UpdateTimestamp
    @Column(name = "updated_date")
    @ToString.Include
    private LocalDateTime updatedDate;

    @Column(name = "section_id", nullable = false)
    @ToString.Include
    private UUID sectionId;

    @Column(name = "student_id", nullable = false)
    @ToString.Include
    private UUID studentId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "scoreDetail", orphanRemoval = true)
    @OrderBy("scoreIndex ASC")
    @Setter(AccessLevel.NONE)
    @ToString.Include
    private Set<ExtraScore> extraScoreSet = new LinkedHashSet<>();

    public void setExtraScoreSet(Set<ExtraScore> newExtraScores) {
        if (newExtraScores == null || newExtraScores.isEmpty()) {
            this.extraScoreSet.clear(); // Xóa sạch nếu form gửi lên list rỗng
            return;
        }

        // 1. Lọc và xóa những cột điểm cũ không còn tồn tại trong list mới gửi lên
        Set<Integer> newIndexes = newExtraScores.stream()
                .map(ExtraScore::getScoreIndex)
                .collect(Collectors.toSet());
        this.extraScoreSet.removeIf(existing -> !newIndexes.contains(existing.getScoreIndex()));

        // 2. Cập nhật điểm cho các cột đã có, hoặc Thêm mới nếu chưa có
        for (ExtraScore incomingScore : newExtraScores) {
            // Tìm xem trong DB đã có cột điểm ở vị trí (index) này chưa
            ExtraScore existingScore = this.extraScoreSet.stream()
                    .filter(es -> es.getScoreIndex().equals(incomingScore.getScoreIndex()))
                    .findFirst()
                    .orElse(null);

            if (existingScore != null) {
                // NẾU ĐÃ CÓ: Chỉ cập nhật giá trị điểm (ID vẫn giữ nguyên -> Hibernate sẽ UPDATE)
                existingScore.setScore(incomingScore.getScore());
            } else {
                // NẾU CHƯA CÓ: Thêm cột mới vào (ID null -> Hibernate sẽ INSERT)
                incomingScore.setScoreDetail(this); // Bắt buộc set khóa ngoại
                this.extraScoreSet.add(incomingScore);
            }
        }
    }
}
