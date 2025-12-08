package com.lqm.academic_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

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
@NamedQueries({
        @NamedQuery(name = "Classroom.findAll", query = "SELECT c FROM Classroom c"),
        @NamedQuery(name = "Classroom.findById", query = "SELECT c FROM Classroom c WHERE c.id = :id"),
        @NamedQuery(name = "Classroom.findByName", query = "SELECT c FROM Classroom c WHERE c.name = :name"),
})
public class Classroom implements Serializable {
    @Id
    @GeneratedValue
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
    private Set<StudentClassroom> studentClassroomSet = new LinkedHashSet<>();

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
