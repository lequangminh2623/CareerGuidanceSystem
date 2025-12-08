package com.lqm.academic_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "semesters")
@NamedQueries({
        @NamedQuery(name = "Semester.findAll", query = "SELECT s FROM Semester s"),
        @NamedQuery(name = "Semester.findById", query = "SELECT s FROM Semester s WHERE s.id = :id"),
        @NamedQuery(name = "Semester.findBySemesterName", query = "SELECT s FROM Semester s WHERE s.name = :semesterName")
})
public class Semester implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Column(name = "name", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private SemesterType name;

    @JoinColumn(name = "year_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    @ToString.Include
    private Year year;

}
