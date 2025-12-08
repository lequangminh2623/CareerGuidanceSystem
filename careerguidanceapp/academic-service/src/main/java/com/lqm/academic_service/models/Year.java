package com.lqm.academic_service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
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
@Table(name = "years")
@NamedQueries({
        @NamedQuery(name = "Year.findAll", query = "SELECT y FROM Year y"),
        @NamedQuery(name = "Year.findById", query = "SELECT y FROM Year y WHERE y.id = :id"),
        @NamedQuery(name = "Year.findByName", query = "SELECT y FROM Year y WHERE y.name = :name"),
})
public class Year implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Pattern(regexp = "^[0-9]{4}-[0-9]{4}$")
    @Column(name = "name", nullable = false, unique = true)
    @ToString.Include
    private String name;

    @Builder.Default
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    @OneToMany(mappedBy = "year", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Include
    private Set<Semester> semesterSet = new LinkedHashSet<>();

    @Builder.Default
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    @OneToMany(mappedBy = "year", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Grade> gradeSet = new LinkedHashSet<>();

}
