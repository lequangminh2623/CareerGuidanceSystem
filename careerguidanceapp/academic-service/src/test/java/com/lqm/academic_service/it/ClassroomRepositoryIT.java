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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test cho {@link com.lqm.academic_service.repositories.ClassroomRepository}.
 *
 * Kiểm tra custom queries: findWithStudentsById, existsByNameAndGradeIdAndIdNot.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ClassroomRepository — Integration Tests")
class ClassroomRepositoryIT extends BaseIntegrationTest {

    @Autowired
    ClassroomRepository classroomRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    YearRepository yearRepository;

    @Autowired
    StudentClassroomRepository studentClassroomRepository;

    private Year year;
    private Grade grade;
    private Classroom classroom;

    @BeforeEach
    void setUp() {
        classroomRepository.deleteAll();
        classroomRepository.flush();
        gradeRepository.deleteAll();
        gradeRepository.flush();
        yearRepository.deleteAll();
        yearRepository.flush();

        year = yearRepository.save(Year.builder().name("2024-2025").build());
        grade = gradeRepository.save(Grade.builder().name(GradeType.GRADE_10).year(year).build());
        classroom = classroomRepository.save(
                Classroom.builder().name("10A1").grade(grade).build());
        classroomRepository.flush();
    }

    // -----------------------------------------------------------------------
    // findById (basic)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findById — trả về Classroom khi ID tồn tại")
    void findById_returnsClassroom_whenIdExists() {
        Optional<Classroom> result = classroomRepository.findById(classroom.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("10A1");
    }

    @Test
    @DisplayName("findById — trả về empty khi ID không tồn tại")
    void findById_returnsEmpty_whenIdNotFound() {
        Optional<Classroom> result = classroomRepository.findById(UUID.randomUUID());
        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // findWithStudentsById
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findWithStudentsById — trả về Classroom cùng danh sách students")
    void findWithStudentsById_returnsClassroomWithStudents() {
        UUID student1 = UUID.randomUUID();
        UUID student2 = UUID.randomUUID();
        // Thêm 2 học sinh vào classroom
        classroom.setStudentClassroomSet(List.of(student1, student2));
        classroomRepository.save(classroom);
        classroomRepository.flush();

        Optional<Classroom> result = classroomRepository.findWithStudentsById(classroom.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStudentClassroomSet()).hasSize(2);
        assertThat(result.get().getStudentClassroomSet())
                .extracting(StudentClassroom::getStudentId)
                .containsExactlyInAnyOrder(student1, student2);
    }

    @Test
    @DisplayName("findWithStudentsById — trả về Classroom rỗng khi không có học sinh")
    void findWithStudentsById_returnsClassroomWithEmptyStudents() {
        Optional<Classroom> result = classroomRepository.findWithStudentsById(classroom.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStudentClassroomSet()).isEmpty();
    }

    @Test
    @DisplayName("findWithStudentsById — trả về empty khi ID không tồn tại")
    void findWithStudentsById_returnsEmpty_whenNotFound() {
        Optional<Classroom> result = classroomRepository.findWithStudentsById(UUID.randomUUID());
        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // existsByNameAndGradeIdAndIdNot
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("existsByNameAndGradeIdAndIdNot — true khi tên trùng với classroom khác cùng grade")
    void existsByNameAndGradeIdAndIdNot_returnsTrue_whenNameDuplicatedAtOtherClassroom() {
        // classroom2 cùng grade, cùng tên
        Classroom classroom2 = classroomRepository.save(
                Classroom.builder().name("10A2").grade(grade).build());

        // Kiểm tra xem "10A1" có tồn tại ở classroom khác classroom2 không → ĐÚNG
        boolean exists = classroomRepository.existsByNameAndGradeIdAndIdNot(
                "10A1", grade.getId(), classroom2.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByNameAndGradeIdAndIdNot — false khi đây chính là classroom đang update")
    void existsByNameAndGradeIdAndIdNot_returnsFalse_whenSameClassroom() {
        boolean exists = classroomRepository.existsByNameAndGradeIdAndIdNot(
                "10A1", grade.getId(), classroom.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByNameAndGradeIdAndIdNot — false khi tên không tồn tại")
    void existsByNameAndGradeIdAndIdNot_returnsFalse_whenNameNotFound() {
        boolean exists = classroomRepository.existsByNameAndGradeIdAndIdNot(
                "99X9", grade.getId(), UUID.randomUUID());
        assertThat(exists).isFalse();
    }

    // -----------------------------------------------------------------------
    // count / deleteAll
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("count — đếm đúng tổng số classroom")
    void count_returnsCorrectTotal() {
        classroomRepository.save(Classroom.builder().name("10A2").grade(grade).build());
        assertThat(classroomRepository.count()).isEqualTo(2L);
    }

    @Test
    @DisplayName("cascade delete — xóa Grade thì Classroom liên quan cũng bị xóa")
    void cascadeDelete_whenGradeDeleted_classroomIsAlsoDeleted() {
        assertThat(classroomRepository.findById(classroom.getId())).isPresent();

        // Xóa tất cả classroom trước để tránh constraint violation
        classroomRepository.deleteAll();
        classroomRepository.flush();

        gradeRepository.deleteById(grade.getId());
        gradeRepository.flush();

        assertThat(gradeRepository.findById(grade.getId())).isEmpty();
        assertThat(classroomRepository.findById(classroom.getId())).isEmpty();
    }
}
