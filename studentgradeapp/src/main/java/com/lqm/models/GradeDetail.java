package com.lqm.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lqm.utils.CollectionUpdater;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "grade_detail")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NamedQueries({
        @NamedQuery(name = "GradeDetail.findAll", query = "SELECT g FROM GradeDetail g"),
        @NamedQuery(name = "GradeDetail.findById", query = "SELECT g FROM GradeDetail g WHERE g.id = :id"),
        @NamedQuery(name = "GradeDetail.findByFinalGrade", query = "SELECT g FROM GradeDetail g WHERE g.finalGrade = :finalGrade"),
        @NamedQuery(name = "GradeDetail.findByMidtermGrade", query = "SELECT g FROM GradeDetail g WHERE g.midtermGrade = :midtermGrade"),
        @NamedQuery(name = "GradeDetail.findByUpdatedDate", query = "SELECT g FROM GradeDetail g WHERE g.updatedDate = :updatedDate")
})
public class GradeDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    @Max(10)
    @Min(0)
    @Column(name = "final_grade")
    private Double finalGrade;

    @Column(name = "midterm_grade")
    private Double midtermGrade;

    @Column(name = "updated_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "UTC")
    private Date updatedDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "gradeDetail", fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("gradeIndex ASC")
    @Setter(AccessLevel.NONE)
    private Set<ExtraGrade> extraGradeSet = new LinkedHashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    private Course course;

    @ManyToOne(optional = false)
    @JoinColumn(name = "semester_id", referencedColumnName = "id")
    private Semester semester;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private Student student;

    // Đồng bộ 2 chiều cho extraGradeSet và gradeDetail
    public void addExtraGrade(ExtraGrade eg) {
        if (eg == null) return;
        extraGradeSet.add(eg);
        eg.setGradeDetail(this);
    }

    public void removeExtraGrade(ExtraGrade eg) {
        if (eg == null) return;
        if (extraGradeSet.remove(eg)) {
            eg.setGradeDetail(null);
        }
    }

    // Setter custom để cập nhật collection đúng cách
    public void setExtraGradeSet(Set<ExtraGrade> newItems) {
        CollectionUpdater.updateSet(
                extraGradeSet,
                newItems,
                this::addExtraGrade,
                this::removeExtraGrade
        );
    }


}
