package com.lqm.user_service.it;

import com.lqm.user_service.models.Role;
import com.lqm.user_service.models.Student;
import com.lqm.user_service.models.User;
import com.lqm.user_service.repositories.UserRepository;
import com.lqm.user_service.specifications.UserSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import com.lqm.user_service.BaseIntegrationTest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test cho {@link UserRepository}.
 *
 * Sử dụng {@code @DataJpaTest} để khởi tạo chỉ JPA layer (không load toàn bộ context),
 * kết hợp Testcontainers PostgreSQL để test với database thực.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UserRepository — Integration Tests")
class UserRepositoryIT extends BaseIntegrationTest {

    @Autowired
    UserRepository userRepository;

    private User teacherUser;
    private User studentUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.flush();

        teacherUser = User.builder()
                .firstName("Nguyen")
                .lastName("Van A")
                .email("teacher.vana@ou.edu.vn")
                .password("encoded-password")
                .gender(true)
                .role(Role.ROLE_TEACHER)
                .active(true)
                .build();

        studentUser = User.builder()
                .firstName("Le")
                .lastName("Thi B")
                .email("student.thib@ou.edu.vn")
                .password("encoded-password")
                .gender(false)
                .role(Role.ROLE_STUDENT)
                .active(true)
                .build();

        Student student = Student.builder()
                .code("2054010001")
                .user(studentUser)
                .build();
        studentUser.setStudent(student);

        userRepository.saveAll(List.of(teacherUser, studentUser));
        userRepository.flush();
    }

    // -----------------------------------------------------------------------
    // findByEmailAndActiveTrue
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findByEmailAndActiveTrue — trả về user khi email tồn tại và active=true")
    void findByEmailAndActiveTrue_returnsUser_whenEmailExistsAndActive() {
        Optional<User> result = userRepository.findByEmailAndActiveTrue("teacher.vana@ou.edu.vn");
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("teacher.vana@ou.edu.vn");
        assertThat(result.get().getRole()).isEqualTo(Role.ROLE_TEACHER);
    }

    @Test
    @DisplayName("findByEmailAndActiveTrue — trả về empty khi email không tồn tại")
    void findByEmailAndActiveTrue_returnsEmpty_whenEmailNotFound() {
        Optional<User> result = userRepository.findByEmailAndActiveTrue("nobody@ou.edu.vn");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByEmailAndActiveTrue — trả về empty khi user bị deactivated")
    void findByEmailAndActiveTrue_returnsEmpty_whenUserIsInactive() {
        teacherUser.setActive(false);
        userRepository.save(teacherUser);

        Optional<User> result = userRepository.findByEmailAndActiveTrue("teacher.vana@ou.edu.vn");
        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // existsByEmailAndExcludeId
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("existsByEmailAndExcludeId — trả về true khi email đã tồn tại ở user khác")
    void existsByEmailAndExcludeId_returnsTrue_whenEmailExistsAtOtherUser() {
        UUID otherId = UUID.randomUUID();
        boolean exists = userRepository.existsByEmailAndExcludeId("teacher.vana@ou.edu.vn", otherId);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmailAndExcludeId — trả về false khi email chính là của user đang update")
    void existsByEmailAndExcludeId_returnsFalse_whenEmailBelongsToExcludedUser() {
        boolean exists = userRepository.existsByEmailAndExcludeId(
                "teacher.vana@ou.edu.vn", teacherUser.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByEmailAndExcludeId — trả về false khi email không tồn tại")
    void existsByEmailAndExcludeId_returnsFalse_whenEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmailAndExcludeId("nobody@ou.edu.vn", null);
        assertThat(exists).isFalse();
    }

    // -----------------------------------------------------------------------
    // countBy
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("countBy — đếm đúng tổng số user")
    void countBy_returnsCorrectTotal() {
        long count = userRepository.countBy();
        assertThat(count).isEqualTo(2L);
    }

    // -----------------------------------------------------------------------
    // countUserByRole
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("countUserByRole — nhóm đúng số user theo role")
    void countUserByRole_returnsGroupedCount() {
        List<Object[]> result = userRepository.countUserByRole();
        assertThat(result).isNotEmpty();

        boolean foundTeacher = result.stream()
                .anyMatch(row -> Role.ROLE_TEACHER.equals(row[0]) && ((Number) row[1]).longValue() == 1L);
        boolean foundStudent = result.stream()
                .anyMatch(row -> Role.ROLE_STUDENT.equals(row[0]) && ((Number) row[1]).longValue() == 1L);

        String debugResult = result.stream()
                .map(row -> Arrays.toString(row))
                .toList().toString();

        assertThat(foundTeacher).as("Expected Teacher count 1. Actual: " + debugResult).isTrue();
        assertThat(foundStudent).as("Expected Student count 1. Actual: " + debugResult).isTrue();
    }

    // -----------------------------------------------------------------------
    // countUserByStatus
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("countUserByStatus — nhóm đúng số user theo active status")
    void countUserByStatus_returnsGroupedCount() {
        List<Object[]> result = userRepository.countUserByStatus();
        assertThat(result).isNotEmpty();

        long activeCount = result.stream()
                .filter(row -> Boolean.TRUE.equals(row[0]))
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();
        assertThat(activeCount).isEqualTo(2L);
    }

    // -----------------------------------------------------------------------
    // findAll với UserSpecification
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findAll(Specification) — filter theo role=Teacher trả đúng user")
    void findAllWithSpec_filterByRole_returnsMatchingUsers() {
        Specification<User> spec = UserSpecification.filterByParams(Map.of("role", "Teacher"));
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository.findAll(spec, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("teacher.vana@ou.edu.vn");
    }

    @Test
    @DisplayName("findAll(Specification) — filter theo keyword tên trả đúng user")
    void findAllWithSpec_filterByKeyword_returnsMatchingUsers() {
        Specification<User> spec = UserSpecification.filterByParams(Map.of("kw", "Van A"));
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository.findAll(spec, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("teacher.vana@ou.edu.vn");
    }

    @Test
    @DisplayName("findAll(Specification) — filter theo active=false không trả kết quả khi tất cả active")
    void findAllWithSpec_filterByActiveTrue_returnsOnlyActiveUsers() {
        Specification<User> spec = UserSpecification.filterByParams(Map.of("active", "false"));
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository.findAll(spec, pageable);

        assertThat(result.getTotalElements()).isEqualTo(0L);
    }

    @Test
    @DisplayName("findAll(Specification) — hasIdIn với danh sách ID hợp lệ")
    void findAllWithSpec_hasIdIn_returnsMatchingUsers() {
        Specification<User> spec = UserSpecification.hasIdIn(List.of(teacherUser.getId()));
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository.findAll(spec, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getId()).isEqualTo(teacherUser.getId());
    }

    @Test
    @DisplayName("findAll(Specification) — hasIdIn với danh sách rỗng trả về empty")
    void findAllWithSpec_hasIdIn_emptyList_returnsEmpty() {
        Specification<User> spec = UserSpecification.hasIdIn(List.of());
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository.findAll(spec, pageable);

        assertThat(result.getTotalElements()).isEqualTo(0L);
    }

    // -----------------------------------------------------------------------
    // Cascade: xóa User → Student bị xóa
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("deleteById — xóa User student thì Student cascade bị xóa")
    void deleteById_cascadeDeletesStudent() {
        UUID studentId = studentUser.getId();
        assertThat(userRepository.findById(studentId)).isPresent();

        userRepository.deleteById(studentId);
        userRepository.flush();

        assertThat(userRepository.findById(studentId)).isEmpty();
    }

    @Test
    @DisplayName("save — lưu User với Student thành công")
    void save_userWithStudent_persistsBoth() {
        User newStudent = User.builder()
                .firstName("Tran")
                .lastName("Van C")
                .email("student.vanc@ou.edu.vn")
                .password("encoded-password")
                .gender(true)
                .role(Role.ROLE_STUDENT)
                .active(true)
                .build();

        Student s = Student.builder().code("2054010099").user(newStudent).build();
        newStudent.setStudent(s);

        User saved = userRepository.save(newStudent);
        userRepository.flush();

        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStudent()).isNotNull();
        assertThat(found.get().getStudent().getCode()).isEqualTo("2054010099");
    }
}
