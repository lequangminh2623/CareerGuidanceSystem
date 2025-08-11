package com.lqm.models;

import com.lqm.utils.CollectionUpdater;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String year;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "academicYear", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Semester> semesterSet = new LinkedHashSet<>();

    public AcademicYear(String year) {
        this.year = year;
    }

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
