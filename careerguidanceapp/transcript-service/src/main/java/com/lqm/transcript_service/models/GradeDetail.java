package com.lqm.transcript_service.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

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
}
