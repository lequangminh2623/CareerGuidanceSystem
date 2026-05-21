package com.lqm.academic_service.it;

import com.lqm.academic_service.BaseIntegrationTest;
import com.lqm.academic_service.models.*;
import com.lqm.academic_service.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test cho {@link com.lqm.academic_service.repositories.SectionRepository}.
 *
 * Kiểm tra các custom queries:
 * - existsByTeacherIdAndId
 * - findCurriculumAndSectionIds
 * - findByTeacherId
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("SectionRepository — Integration Tests")
class SectionRepositoryIT extends BaseIntegrationTest {

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    ClassroomRepository classroomRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    YearRepository yearRepository;

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    CurriculumRepository curriculumRepository;

    private UUID teacherId;
    private Classroom classroom;
    private Curriculum curriculum1;
    private Curriculum curriculum2;
    private Section section1;
    private Section section2;

    @BeforeEach
    void setUp() {
        sectionRepository.deleteAll();
        sectionRepository.flush();
        classroomRepository.deleteAll();
        classroomRepository.flush();
        curriculumRepository.deleteAll();
        curriculumRepository.flush();
        gradeRepository.deleteAll();
        gradeRepository.flush();
        semesterRepository.deleteAll();
        semesterRepository.flush();
        subjectRepository.deleteAll();
        subjectRepository.flush();
        yearRepository.deleteAll();
        yearRepository.flush();

        teacherId = UUID.randomUUID();

        Year year = yearRepository.save(Year.builder().name("2024-2025").build());
        Grade grade = gradeRepository.save(Grade.builder().name(GradeType.GRADE_10).year(year).build());
        Semester semester = semesterRepository.save(
                Semester.builder().name(SemesterType.SEMESTER_1).year(year).build());
        Subject subjectMath = subjectRepository.save(Subject.builder().name("Toán").build());
        Subject subjectLit = subjectRepository.save(Subject.builder().name("Văn").build());

        classroom = classroomRepository.save(Classroom.builder().name("10A1").grade(grade).build());

        curriculum1 = curriculumRepository.save(
                Curriculum.builder().grade(grade).semester(semester).subject(subjectMath).build());
        curriculum2 = curriculumRepository.save(
                Curriculum.builder().grade(grade).semester(semester).subject(subjectLit).build());

        section1 = sectionRepository.save(Section.builder()
                .teacherId(teacherId)
                .scoreStatus(ScoreStatusType.DRAFT)
                .classroom(classroom)
                .curriculum(curriculum1)
                .build());

        section2 = sectionRepository.save(Section.builder()
                .teacherId(UUID.randomUUID()) // teacher khác
                .scoreStatus(ScoreStatusType.DRAFT)
                .classroom(classroom)
                .curriculum(curriculum2)
                .build());

        sectionRepository.flush();
    }

    // -----------------------------------------------------------------------
    // existsByTeacherIdAndId
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("existsByTeacherIdAndId — true khi đúng cặp teacherId + sectionId")
    void existsByTeacherIdAndId_returnsTrue_whenMatch() {
        boolean exists = sectionRepository.existsByTeacherIdAndId(teacherId, section1.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByTeacherIdAndId — false khi teacherId không khớp")
    void existsByTeacherIdAndId_returnsFalse_whenTeacherMismatch() {
        boolean exists = sectionRepository.existsByTeacherIdAndId(UUID.randomUUID(), section1.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByTeacherIdAndId — false khi sectionId không tồn tại")
    void existsByTeacherIdAndId_returnsFalse_whenSectionNotFound() {
        boolean exists = sectionRepository.existsByTeacherIdAndId(teacherId, UUID.randomUUID());
        assertThat(exists).isFalse();
    }

    // -----------------------------------------------------------------------
    // findCurriculumAndSectionIds
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findCurriculumAndSectionIds — trả về đúng cặp (curriculumId, sectionId)")
    void findCurriculumAndSectionIds_returnsCorrectPairs() {
        List<Object[]> result = sectionRepository.findCurriculumAndSectionIds(
                classroom.getId(), Set.of(curriculum1.getId(), curriculum2.getId()));

        assertThat(result).hasSize(2);

        boolean foundSection1 = result.stream().anyMatch(row ->
                curriculum1.getId().equals(row[0]) && section1.getId().equals(row[1]));
        boolean foundSection2 = result.stream().anyMatch(row ->
                curriculum2.getId().equals(row[0]) && section2.getId().equals(row[1]));

        assertThat(foundSection1).isTrue();
        assertThat(foundSection2).isTrue();
    }

    @Test
    @DisplayName("findCurriculumAndSectionIds — trả về rỗng khi không khớp classroomId")
    void findCurriculumAndSectionIds_returnsEmpty_whenClassroomMismatch() {
        List<Object[]> result = sectionRepository.findCurriculumAndSectionIds(
                UUID.randomUUID(), Set.of(curriculum1.getId()));
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findCurriculumAndSectionIds — trả về rỗng khi curriculumIds rỗng")
    void findCurriculumAndSectionIds_returnsEmpty_whenCurriculumIdsEmpty() {
        List<Object[]> result = sectionRepository.findCurriculumAndSectionIds(
                classroom.getId(), Set.of());
        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // findByTeacherId
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findByTeacherId — trả về đúng section của teacher")
    void findByTeacherId_returnsSectionsForTeacher() {
        List<Section> result = sectionRepository.findByTeacherId(teacherId);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(section1.getId());
    }

    @Test
    @DisplayName("findByTeacherId — trả về rỗng khi teacherId không tồn tại")
    void findByTeacherId_returnsEmpty_whenTeacherNotFound() {
        List<Section> result = sectionRepository.findByTeacherId(UUID.randomUUID());
        assertThat(result).isEmpty();
    }
}
