package com.lqm.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "extra_grade")
@NamedQueries({
        @NamedQuery(name = "ExtraGrade.findAll", query = "SELECT e FROM ExtraGrade e"),
        @NamedQuery(name = "ExtraGrade.findById", query = "SELECT e FROM ExtraGrade e WHERE e.id = :id"),
        @NamedQuery(name = "ExtraGrade.findByGrade", query = "SELECT e FROM ExtraGrade e WHERE e.grade = :grade")
})
public class ExtraGrade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    @Setter(AccessLevel.NONE) // Khóa setter, chỉ đọc
    private Integer id;

    @Max(10)
    @Min(0)
    @Column(name = "grade")
    private Double grade;

    @Column(name = "grade_index", nullable = false)
    private Integer gradeIndex;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grade_detail_id", referencedColumnName = "id")
    @JsonIgnore
    @Setter(AccessLevel.NONE) // Khóa setter auto, sẽ có setter custom
    private GradeDetail gradeDetail;

    public ExtraGrade() {
    }

    public ExtraGrade(Integer id) {
        this.id = id;
    }

    /**
     * Setter custom để gắn ExtraGrade vào GradeDetail và đồng bộ 2 chiều.
     */
    public void setGradeDetail(GradeDetail gradeDetail) {
        if (Objects.equals(this.gradeDetail, gradeDetail)) {
            return;
        }
        if (this.gradeDetail != null) {
            this.gradeDetail.getExtraGradeSet().remove(this);
        }
        this.gradeDetail = gradeDetail;
        if (gradeDetail != null) {
            gradeDetail.getExtraGradeSet().add(this);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ExtraGrade other)) return false;
        return Objects.equals(id, other.id)
                && Objects.equals(gradeIndex, other.gradeIndex)
                && Objects.equals(grade, other.grade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, gradeIndex, grade);
    }

    @Override
    public String toString() {
        return "ExtraGrade[ id=" + id + " ]";
    }
}
