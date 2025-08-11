package com.lqm.repositories;

import com.lqm.models.Classroom;
import com.lqm.models.Student;
import com.lqm.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Integer>, JpaSpecificationExecutor<Classroom> {

    // Kiểm tra tồn tại lớp học trùng tên trong học kỳ và khóa học
    boolean existsByNameAndSemesterIdAndCourseId(String name, Integer semesterId, Integer courseId);

    boolean existsByNameAndSemesterIdAndCourseIdAndIdNot(String name, Integer semesterId, Integer courseId, Integer excludeId);

    // Kiểm tra sinh viên đã có mặt trong lớp khác của học kỳ và khóa học
    boolean existsByStudentSet_IdAndSemester_IdAndCourse_Id(int studentId, int semesterId, int courseId);

    boolean existsByStudentSet_IdAndSemester_IdAndCourse_IdAndIdNot(int studentId, int semesterId, int courseId, int excludeClassroomId);

    // Kiểm tra người dùng (sinh viên hoặc giảng viên) có trong lớp không
    boolean existsByStudentSet_User_IdAndId(int userId, int classroomId);

    // Tìm lớp học theo giảng viên
    List<Classroom> findByLecturer(User user);

    // Lấy lớp học kèm sinh viên
    @Query("SELECT c FROM Classroom c LEFT JOIN FETCH c.studentSet WHERE c.id = :id")
    Classroom findWithStudentsById(@Param("id") Integer id);

    // Lấy lớp học theo bài viết diễn đàn
    @Query("SELECT fp.classroom FROM ForumPost fp WHERE fp.id = :postId")
    Classroom findByForumPostId(@Param("postId") int postId);

    // Lấy sinh viên trong lớp học (có phân trang)
    @Query("SELECT s FROM Student s JOIN s.classroomSet c WHERE c.id = :classroomId")
    Page<Student> findStudentsInClassroom(@Param("classroomId") Integer classroomId, Pageable pageable);

    // Đếm số lượng sinh viên trong lớp
    @Query("SELECT COUNT(s) FROM Student s JOIN s.classroomSet c WHERE c.id = :classroomId")
    long countStudentsInClassroom(@Param("classroomId") Integer classroomId);


}
