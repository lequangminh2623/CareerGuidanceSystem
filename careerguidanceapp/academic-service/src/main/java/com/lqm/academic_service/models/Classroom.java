package com.lqm.academic_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "classrooms")
public class Classroom implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Size(min = 1, max = 255)
    @Column(name = "name", nullable = false)
    @ToString.Include
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grade_id", referencedColumnName = "id")
    @ToString.Include
    private Grade grade;

    @Builder.Default
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Section> sectionSet = new LinkedHashSet<>();

    @Builder.Default
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentClassroom> studentClassroomSet = new LinkedHashSet<>();

    @Formula("(SELECT COUNT(*) FROM students_classrooms sc WHERE sc.classroom_id = id)")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Integer studentCount;

    public Integer getStudentCount() {
        return studentCount != null ? studentCount : 0;
    }

    public void addStudent(StudentClassroom studentClassroom) {
        if (studentClassroom == null) return;
        studentClassroomSet.add(studentClassroom);
        studentClassroom.setClassroom(this);
    }

    public void removeStudent(StudentClassroom studentClassroom) {
        if (studentClassroom == null) return;
        if (studentClassroomSet.remove(studentClassroom)) {
            studentClassroom.setClassroom(null);
        }
    }

    public void setStudentClassroomSet(List<UUID> newStudentIds) {
        // Xóa toàn bộ nếu newSet null
        if (newStudentIds == null) {
            for (StudentClassroom item : new LinkedHashSet<>(studentClassroomSet)) {
                removeStudent(item);
            }
            return;
        }

        Set<StudentClassroom> newSet = newStudentIds.stream().map(
                studentId -> StudentClassroom.builder()
                        .studentId(studentId)
                        .classroom(this)
                        .build()
                ).collect(Collectors.toSet());

        // Xóa phần tử không còn
        for (StudentClassroom item : new LinkedHashSet<>(studentClassroomSet)) {
            if (!newSet.contains(item)) {
                removeStudent(item);
            }
        }

        // Thêm phần tử mới
        for (StudentClassroom item : newSet) {
            if (!studentClassroomSet.contains(item)) {
                addStudent(item);
            }
        }
    }

}
