package com.lqm.models;

import com.lqm.utils.CollectionUpdater;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "course")
@NamedQueries({
        @NamedQuery(name = "Course.findAll", query = "SELECT c FROM Course c"),
        @NamedQuery(name = "Course.findById", query = "SELECT c FROM Course c WHERE c.id = :id"),
        @NamedQuery(name = "Course.findByName", query = "SELECT c FROM Course c WHERE c.name = :name")
})
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    @Setter(AccessLevel.NONE)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "name", nullable = false)
    @ToString.Include
    private String name;

    @NotNull
    @Min(value = 0, message = "{course.credit.invalid}")
    @Max(value = 7, message = "{course.credit.invalid}")
    @Column(name = "credit", nullable = false)
    private int credit;

    // Quan hệ 1-n với GradeDetail (không để Lombok sinh setter cho collection)
    @OneToMany(mappedBy = "course")
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private Set<GradeDetail> gradeDetailSet = new LinkedHashSet<>();

    // Quan hệ 1-n với Classroom
    @OneToMany(mappedBy = "course")
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private Set<Classroom> classroomSet = new LinkedHashSet<>();

    // --- constructors tương thích ---
    public Course(Integer id) {
        this.id = id;
    }

    public Course(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    // ===== GradeDetail management =====
    public void addGradeDetail(GradeDetail gd) {
        if (gd == null) return;
        if (gradeDetailSet.add(gd)) {
            gd.setCourse(this);
        }
    }

    public void removeGradeDetail(GradeDetail gd) {
        if (gd == null) return;
        if (gradeDetailSet.remove(gd)) {
            gd.setCourse(null);
        }
    }

    public void setGradeDetailSet(Set<GradeDetail> newItems) {
        CollectionUpdater.updateSet(
                gradeDetailSet,
                newItems,
                this::addGradeDetail,
                this::removeGradeDetail
        );
    }

    // ===== Classroom management =====
    public void addClassroom(Classroom c) {
        if (c == null) return;
        if (classroomSet.add(c)) {
            c.setCourse(this);
        }
    }

    public void removeClassroom(Classroom c) {
        if (c == null) return;
        if (classroomSet.remove(c)) {
            c.setCourse(null);
        }
    }

    public void setClassroomSet(Set<Classroom> newItems) {
        CollectionUpdater.updateSet(
                classroomSet,
                newItems,
                this::addClassroom,
                this::removeClassroom
        );
    }
}
