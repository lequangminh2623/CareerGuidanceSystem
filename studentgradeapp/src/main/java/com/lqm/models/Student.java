package com.lqm.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lqm.utils.CollectionUpdater;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "student")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NamedQueries({
        @NamedQuery(name = "Student.findAll", query = "SELECT s FROM Student s"),
        @NamedQuery(name = "Student.findById", query = "SELECT s FROM Student s WHERE s.id = :id"),
        @NamedQuery(name = "Student.findByCode", query = "SELECT s FROM Student s WHERE s.code = :code")
})
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer id;

    @Basic(optional = false)
    @NotNull
    @Size(min = 10, max = 10, message = "{user.student.code.size}")
    @Column(name = "code")
    private String code;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonIgnore
    private User user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "student", orphanRemoval = true)
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private Set<GradeDetail> gradeDetailSet = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "studentSet")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Setter(AccessLevel.NONE)
    private Set<Classroom> classroomSet = new LinkedHashSet<>();

    public Student() {}

    public Student(Integer id) {
        this.id = id;
    }

    public Student(Integer id, String code) {
        this.id = id;
        this.code = code;
    }

    // Đồng bộ 2 chiều cho gradeDetailSet
    public void addGradeDetail(GradeDetail gradeDetail) {
        if (gradeDetail == null) return;
        gradeDetailSet.add(gradeDetail);
        gradeDetail.setStudent(this);
    }

    public void removeGradeDetail(GradeDetail gradeDetail) {
        if (gradeDetail == null) return;
        if (gradeDetailSet.remove(gradeDetail)) {
            gradeDetail.setStudent(null);
        }
    }

    public void setGradeDetailSet(Set<GradeDetail> newGradeDetails) {
        CollectionUpdater.updateSet(
                gradeDetailSet,
                newGradeDetails,
                this::addGradeDetail,
                this::removeGradeDetail
        );
    }

    // Đồng bộ 2 chiều cho classroomSet
    public void addClassroom(Classroom classroom) {
        if (classroom == null) return;
        classroomSet.add(classroom);
        classroom.getStudentSet().add(this);
    }

    public void removeClassroom(Classroom classroom) {
        if (classroom == null) return;
        if (classroomSet.remove(classroom)) {
            classroom.getStudentSet().remove(this);
        }
    }

    public void setClassroomSet(Set<Classroom> newClassrooms) {
        CollectionUpdater.updateSet(
                classroomSet,
                newClassrooms,
                this::addClassroom,
                this::removeClassroom
        );
    }

}
