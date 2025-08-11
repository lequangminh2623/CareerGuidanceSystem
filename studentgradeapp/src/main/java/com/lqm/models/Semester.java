package com.lqm.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lqm.utils.CollectionUpdater;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "semester")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NamedQueries({
        @NamedQuery(name = "Semester.findAll", query = "SELECT s FROM Semester s"),
        @NamedQuery(name = "Semester.findById", query = "SELECT s FROM Semester s WHERE s.id = :id"),
        @NamedQuery(name = "Semester.findBySemesterType", query = "SELECT s FROM Semester s WHERE s.semesterType = :semesterType")
})
public class Semester implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    @Basic(optional = false)
    @NotNull
    @Column(name = "semester_type")
    private String semesterType;

    @OneToMany(mappedBy = "semester")
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private Set<GradeDetail> gradeDetailSet = new LinkedHashSet<>();

    @OneToMany(mappedBy = "semester")
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private Set<Classroom> classroomSet = new LinkedHashSet<>();

    @JoinColumn(name = "academic_year_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private AcademicYear academicYear;

    public Semester() {}

    public Semester(Integer id) {
        this.id = id;
    }

    public Semester(Integer id, String semesterType) {
        this.id = id;
        this.semesterType = semesterType;
    }

    // --- Đồng bộ 2 chiều cho gradeDetailSet ---
    public void addGradeDetail(GradeDetail gradeDetail) {
        if (gradeDetail == null) return;
        gradeDetailSet.add(gradeDetail);
        gradeDetail.setSemester(this);
    }

    public void removeGradeDetail(GradeDetail gradeDetail) {
        if (gradeDetail == null) return;
        if (gradeDetailSet.remove(gradeDetail)) {
            gradeDetail.setSemester(null);
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

    // --- Đồng bộ 2 chiều cho classroomSet ---
    public void addClassroom(Classroom classroom) {
        if (classroom == null) return;
        classroomSet.add(classroom);
        classroom.setSemester(this);
    }

    public void removeClassroom(Classroom classroom) {
        if (classroom == null) return;
        if (classroomSet.remove(classroom)) {
            classroom.setSemester(null);
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
