package com.lqm.models;

import com.lqm.utils.CollectionUpdater;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class AcademicYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer id;

    @Basic(optional = false)
    @NotNull
    @Pattern(regexp = "^[0-9]{4}-[0-9]{4}$", message = "Năm học phải có định dạng yyyy-yyyy")
    @Column(name = "year")
    private String year;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "academicYear", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Semester> semesterSet = new LinkedHashSet<>();

    // Thêm 1 semester vào AcademicYear
    public void addSemester(Semester semester) {
        if (semester == null) return;
        semesterSet.add(semester);
        semester.setAcademicYear(this);
    }

    // Xóa 1 semester khỏi AcademicYear
    public void removeSemester(Semester semester) {
        if (semester == null) return;
        semesterSet.remove(semester);
        semester.setAcademicYear(null);
    }

    // Custom setter để đảm bảo JPA cập nhật đúng
    public void setSemesterSet(Set<Semester> newItems) {
        CollectionUpdater.updateSet(
                semesterSet,
                newItems,
                this::addSemester,     // Cách thêm
                this::removeSemester   // Cách xóa
        );
    }
}
