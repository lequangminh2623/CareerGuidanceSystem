package com.lqm.services.impl;

import com.lqm.models.Semester;
import com.lqm.repositories.SemesterRepository;
import com.lqm.services.SemesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class SemesterServiceImpl implements SemesterService {

    private final SemesterRepository semesterRepo;

    @Autowired
    public SemesterServiceImpl(SemesterRepository semesterRepo) {
        this.semesterRepo = semesterRepo;
    }

    @Override
    public List<Semester> getSemestersByAcademicYearId(int id, Map<String, String> params) {
        String kw = (params != null) ? params.get("kw") : null;
        return semesterRepo.findByAcademicYearId(id, kw);
    }

    @Override
    public List<Semester> getSemesters(Map<String, String> params) {
        String kw = (params != null) ? params.get("kw") : null;
        return semesterRepo.findAllByKeyword(kw);
    }

    @Override
    public Semester saveSemester(Semester semester) {
        return semesterRepo.save(semester);
    }

    @Override
    public Semester getSemesterById(int id) {
        Optional<Semester> opt = semesterRepo.findById(id);
        return opt.orElse(null);
    }

    @Override
    public void deleteSemesterById(int id) {
        semesterRepo.deleteById(id);
    }

    @Override
    public boolean existSemesterByTypeAndAcademicYearId(String type, Integer semesterId, Integer yearId) {
        if (semesterId == null) {
            return semesterRepo.existsBySemesterTypeAndAcademicYearId(type, yearId);
        } else {
            return semesterRepo.existsBySemesterTypeAndAcademicYearIdAndIdNot(type, yearId, semesterId);
        }
    }
}
