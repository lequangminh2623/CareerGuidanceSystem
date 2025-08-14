package com.lqm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "classroom")
@NamedQueries({
        @NamedQuery(name = "Classroom.findAll", query = "SELECT c FROM Classroom c"),
        @NamedQuery(name = "Classroom.findById", query = "SELECT c FROM Classroom c WHERE c.id = :id"),
        @NamedQuery(name = "Classroom.findByName", query = "SELECT c FROM Classroom c WHERE c.name = :name"),
        @NamedQuery(name = "Classroom.findByGradeStatus", query = "SELECT c FROM Classroom c WHERE c.gradeStatus = :gradeStatus")
})
public class Classroom implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @Column(name = "id")
    @Basic(optional = false)
    @EqualsAndHashCode.Include
    private Integer id;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "name")
    @ToString.Include
    private String name;

    @NotNull
    @Size(max = 6)
    @Column(name = "grade_status")
    private String gradeStatus = "DRAFT";

    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL)
    @Setter(AccessLevel.NONE)
    private Set<ForumPost> forumPostSet = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "classroom_student",
            joinColumns = @JoinColumn(name = "classroom_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @Setter(AccessLevel.NONE)
    private Set<Student> studentSet = new HashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    private Course course;

    @ManyToOne(optional = false)
    @JoinColumn(name = "semester_id", referencedColumnName = "id")
    private Semester semester;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lecturer_id", referencedColumnName = "id")
    private User lecturer;

    // ----- ForumPost management -----
    public void addForumPost(ForumPost post) {
        if (post == null) return;
        forumPostSet.add(post);
        post.setClassroom(this);
    }

    public void removeForumPost(ForumPost post) {
        if (post == null) return;
        if (forumPostSet.remove(post)) {
            post.setClassroom(null);
        }
    }

    public void setForumPostSet(Set<ForumPost> newItems) {
        forumPostSet.clear();
        if (newItems != null) {
            for (ForumPost post : newItems) {
                addForumPost(post);
            }
        }
    }

    // ----- Student management -----
    public void addStudent(Student student) {
        if (student == null) return;
        studentSet.add(student);
    }

    public void removeStudent(Student student) {
        if (student == null) return;
        studentSet.remove(student);
    }

    public void setStudentSet(Set<Student> newItems) {
        studentSet.clear();
        if (newItems != null) {
            studentSet.addAll(newItems);
        }
    }

    public void clearStudents() {
        studentSet.clear();
    }
}
