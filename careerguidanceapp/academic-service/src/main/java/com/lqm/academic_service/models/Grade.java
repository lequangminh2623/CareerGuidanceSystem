package com.lqm.academic_service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "grades")
@NamedQueries({
        @NamedQuery(name = "Grade.findAll", query = "SELECT g FROM Grade g"),
        @NamedQuery(name = "Grade.findById", query = "SELECT g FROM Grade g WHERE g.id = :id"),
        @NamedQuery(name = "Grade.findByName", query = "SELECT g FROM Grade g WHERE g.name = :name"),
})
public class Grade implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Column(name = "name", nullable = false, length = 8)
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private GradeType name;

    @JoinColumn(name = "year_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    @ToString.Include
    private Year year;

    @Builder.Default
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    @OneToMany(mappedBy = "grade", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Classroom> classroomSet = new LinkedHashSet<>();
}
