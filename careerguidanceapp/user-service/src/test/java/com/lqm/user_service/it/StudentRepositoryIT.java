package com.lqm.user_service.it;

import com.lqm.user_service.models.Role;
import com.lqm.user_service.models.Student;
import com.lqm.user_service.models.User;
import com.lqm.user_service.repositories.StudentRepository;
import com.lqm.user_service.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import com.lqm.user_service.BaseIntegrationTest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test cho {@link StudentRepository}.
 *
 * Kiểm tra các query method tùy chỉnh và hành vi cascade từ User.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("StudentRepository — Integration Tests")
class StudentRepositoryIT extends BaseIntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StudentRepository studentRepository;

    private User studentUser1;
    private User studentUser2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.flush();

        studentUser1 = User.builder()
                .firstName("Nguyen")
                .lastName("Van A")
                .email("student.vana@ou.edu.vn")
                .password("encoded-password")
                .gender(true)
                .role(Role.ROLE_STUDENT)
                .active(true)
                .build();
        Student s1 = Student.builder().code("2054010001").user(studentUser1).build();
        studentUser1.setStudent(s1);

        studentUser2 = User.builder()
                .firstName("Le")
                .lastName("Thi B")
                .email("student.thib@ou.edu.vn")
                .password("encoded-password")
                .gender(false)
                .role(Role.ROLE_STUDENT)
                .active(true)
                .build();
        Student s2 = Student.builder().code("2054010002").user(studentUser2).build();
        studentUser2.setStudent(s2);

        userRepository.save(studentUser1);
        userRepository.save(studentUser2);
        userRepository.flush();
    }

    // -----------------------------------------------------------------------
    // findById (kế thừa JpaRepository — kiểm tra Student được persist đúng)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findById — trả về Student khi ID tồn tại")
    void findById_returnsStudent_whenIdExists() {
        Optional<Student> result = studentRepository.findById(studentUser1.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("2054010001");
    }

    @Test
    @DisplayName("findById — trả về empty khi ID không tồn tại")
    void findById_returnsEmpty_whenIdNotFound() {
        Optional<Student> result = studentRepository.findById(UUID.randomUUID());
        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // existsByCodeAndUserIdNot
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("existsByCodeAndUserIdNot — true khi code trùng với user khác")
    void existsByCodeAndUserIdNot_returnsTrue_whenCodeExistsAtOtherUser() {
        // Code "2054010001" của studentUser1 — kiểm tra từ góc nhìn studentUser2
        boolean exists = studentRepository.existsByCodeAndUserIdNot(
                "2054010001", studentUser2.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByCodeAndUserIdNot — false khi code thuộc chính user đang update")
    void existsByCodeAndUserIdNot_returnsFalse_whenCodeBelongsToSameUser() {
        boolean exists = studentRepository.existsByCodeAndUserIdNot(
                "2054010001", studentUser1.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByCodeAndUserIdNot — false khi code không tồn tại")
    void existsByCodeAndUserIdNot_returnsFalse_whenCodeNotFound() {
        boolean exists = studentRepository.existsByCodeAndUserIdNot(
                "9999999999", UUID.randomUUID());
        assertThat(exists).isFalse();
    }

    // -----------------------------------------------------------------------
    // Cascade: xóa User → Student bị xóa (orphanRemoval)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("cascade delete — xóa User thì Student liên quan cũng bị xóa")
    void cascadeDelete_whenUserDeleted_studentIsAlsoDeleted() {
        UUID userId = studentUser1.getId();

        // Verify student tồn tại trước khi xóa
        assertThat(studentRepository.findById(userId)).isPresent();

        // Xóa user
        userRepository.deleteById(userId);
        userRepository.flush();

        // Student phải bị xóa theo cascade
        assertThat(studentRepository.findById(userId)).isEmpty();
    }

    // -----------------------------------------------------------------------
    // count
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("count — đếm đúng tổng số student")
    void count_returnsCorrectTotal() {
        long count = studentRepository.count();
        assertThat(count).isEqualTo(2L);
    }
}
