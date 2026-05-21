package com.lqm.attendance_service.it;

import com.lqm.attendance_service.BaseIntegrationTest;
import com.lqm.attendance_service.models.Fingerprint;
import com.lqm.attendance_service.repositories.FingerprintRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("FingerprintRepository — Integration Tests")
class FingerprintRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private FingerprintRepository fingerprintRepository;

    private UUID classroomId;
    private UUID student1;
    private UUID student2;

    @BeforeEach
    void setUp() {
        fingerprintRepository.deleteAll();
        fingerprintRepository.flush();

        classroomId = UUID.randomUUID();
        student1 = UUID.randomUUID();
        student2 = UUID.randomUUID();
    }

    @Test
    @DisplayName("findByFingerprintIndexAndClassroomId — Tìm fingerprint theo chỉ mục và lớp")
    void findByFingerprintIndexAndClassroomId_ReturnsFingerprint() {
        Fingerprint fp = Fingerprint.builder()
                .fingerprintIndex(1)
                .classroomId(classroomId)
                .studentId(student1)
                .build();
        fingerprintRepository.save(fp);
        fingerprintRepository.flush();

        Optional<Fingerprint> result = fingerprintRepository.findByFingerprintIndexAndClassroomId(1, classroomId);
        assertThat(result).isPresent();
        assertThat(result.get().getStudentId()).isEqualTo(student1);

        assertThat(fingerprintRepository.findByFingerprintIndexAndClassroomId(2, classroomId)).isEmpty();
    }

    @Test
    @DisplayName("findByStudentIdAndClassroomId — Tìm fingerprint theo học sinh và lớp")
    void findByStudentIdAndClassroomId_ReturnsFingerprint() {
        Fingerprint fp = Fingerprint.builder()
                .fingerprintIndex(1)
                .classroomId(classroomId)
                .studentId(student1)
                .build();
        fingerprintRepository.save(fp);
        fingerprintRepository.flush();

        Optional<Fingerprint> result = fingerprintRepository.findByStudentIdAndClassroomId(student1, classroomId);
        assertThat(result).isPresent();
        assertThat(result.get().getFingerprintIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("findAllStudentAndClassroomMappings / findAllFingerprintMappings")
    void findAllMappings_ReturnsMappings() {
        Fingerprint fp1 = Fingerprint.builder()
                .fingerprintIndex(1)
                .classroomId(classroomId)
                .studentId(student1)
                .build();
        Fingerprint fp2 = Fingerprint.builder()
                .fingerprintIndex(2)
                .classroomId(classroomId)
                .studentId(student2)
                .build();
        fingerprintRepository.saveAll(List.of(fp1, fp2));
        fingerprintRepository.flush();

        List<Object[]> mappings = fingerprintRepository.findAllStudentAndClassroomMappings();
        assertThat(mappings).hasSize(2);

        List<Object[]> fingerprintMappings = fingerprintRepository.findAllFingerprintMappings();
        assertThat(fingerprintMappings).hasSize(2);
    }

    @Test
    @DisplayName("deleteByClassroomIdAndStudentIdIn — Xoá hàng loạt fingerprint của học sinh cụ thể")
    void deleteByClassroomIdAndStudentIdIn_DeletesSpecifiedFingerprints() {
        Fingerprint fp1 = Fingerprint.builder()
                .fingerprintIndex(1)
                .classroomId(classroomId)
                .studentId(student1)
                .build();
        Fingerprint fp2 = Fingerprint.builder()
                .fingerprintIndex(2)
                .classroomId(classroomId)
                .studentId(student2)
                .build();
        fingerprintRepository.saveAll(List.of(fp1, fp2));
        fingerprintRepository.flush();

        fingerprintRepository.deleteByClassroomIdAndStudentIdIn(classroomId, List.of(student1));
        fingerprintRepository.flush();

        assertThat(fingerprintRepository.findByStudentIdAndClassroomId(student1, classroomId)).isEmpty();
        assertThat(fingerprintRepository.findByStudentIdAndClassroomId(student2, classroomId)).isPresent();
    }

    @Test
    @DisplayName("deleteByClassroomId — Xoá tất cả fingerprint của lớp")
    void deleteByClassroomId_DeletesAllForClassroom() {
        Fingerprint fp1 = Fingerprint.builder()
                .fingerprintIndex(1)
                .classroomId(classroomId)
                .studentId(student1)
                .build();
        Fingerprint fp2 = Fingerprint.builder()
                .fingerprintIndex(2)
                .classroomId(classroomId)
                .studentId(student2)
                .build();
        fingerprintRepository.saveAll(List.of(fp1, fp2));
        fingerprintRepository.flush();

        fingerprintRepository.deleteByClassroomId(classroomId);
        fingerprintRepository.flush();

        assertThat(fingerprintRepository.findByClassroomIdAndStudentIdIn(classroomId, List.of(student1, student2))).isEmpty();
    }
}
