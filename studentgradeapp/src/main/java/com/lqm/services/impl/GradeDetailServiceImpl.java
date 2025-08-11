package com.lqm.services.impl;

import com.lqm.dtos.*;
import com.lqm.models.*;
import com.lqm.repositories.*;
import com.lqm.services.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import smile.clustering.KMeans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static com.lqm.specifications.GradeDetailSpecification.filter;

@Service
@Transactional
public class GradeDetailServiceImpl implements GradeDetailService {

    private static final int MAX_EXTRA_GRADES = 3;

    @Autowired
    private GradeDetailRepository gradeDetailRepo;

    @Autowired
    private ClassroomRepository classroomRepo;

    @Autowired
    private StudentService studentService;

    @Lazy
    @Autowired
    private ClassroomService classroomService;

    @Override
    @Transactional(readOnly = true)
    public List<GradeDetail> getGradeDetail(Map<String, Integer> params) {
        Classroom classroom = null;
        if (params != null && params.containsKey("classroomId")) {
            classroom = classroomRepo.findById(params.get("classroomId")).orElse(null);
        }
        Specification<GradeDetail> spec = filter(params != null ? params : Collections.emptyMap(), classroom);
        return gradeDetailRepo.findAll(spec);
    }

    @Override
    public void deleteGradeDetail(Integer id) {
        gradeDetailRepo.deleteById(id);
    }

    @Override
    public void saveGradeDetail(GradeDetail gd) {
        gradeDetailRepo.save(gd);
    }

    @Override
    public void saveGradesForStudent(Integer studentId, Integer classroomId, Double midtermGrade,
                                     Double finalGrade, List<Double> extraGrades) {
        Student student = studentService.getStudentByUserId(studentId);
        Classroom classroom = classroomService.getClassroomById(classroomId);
        if (student == null || classroom == null) {
            throw new IllegalArgumentException("Sinh viên hoặc lớp không tồn tại");
        }
        if (!classroomService.existUserInClassroom(studentId, classroomId)) {
            throw new IllegalArgumentException("Sinh viên không thuộc lớp");
        }

        // tìm hoặc tạo GradeDetail
        Map<String, Integer> params = new HashMap<>();
        params.put("studentId", studentId);
        params.put("classroomId", classroomId);
        List<GradeDetail> list = getGradeDetail(params);

        GradeDetail gd;
        if (list.isEmpty()) {
            gd = new GradeDetail();
            gd.setStudent(student);
            gd.setCourse(classroom.getCourse());
            gd.setSemester(classroom.getSemester());
        } else {
            gd = list.get(0);
        }

        gd.setMidtermGrade(midtermGrade);
        gd.setFinalGrade(finalGrade);
        gd.setUpdatedDate(new Date());

        // xử lý điểm bổ sung: tái sử dụng ExtraGrade hiện có theo gradeIndex
        Set<ExtraGrade> newSet = new LinkedHashSet<>();
        if (extraGrades != null) {
            // map hiện tại: gradeIndex -> ExtraGrade
            Map<Integer, ExtraGrade> exist = Optional.ofNullable(gd.getExtraGradeSet())
                    .orElse(Collections.emptySet())
                    .stream()
                    .collect(Collectors.toMap(ExtraGrade::getGradeIndex, eg -> eg));

            for (int i = 0; i < extraGrades.size(); i++) {
                Double val = checkValidGrade(extraGrades.get(i));
                ExtraGrade eg = exist.getOrDefault(i, new ExtraGrade());
                eg.setGradeIndex(i);
                eg.setGrade(val);
                newSet.add(eg);
            }
        }

        gd.setExtraGradeSet(newSet);

        saveGradeDetail(gd);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByStudentAndCourseAndSemester(Integer studentId, Integer courseId,
                                                       Integer semesterId, Integer excludeId) {
        if (excludeId != null) {
            return gradeDetailRepo.existsByStudentIdAndCourseIdAndSemesterIdAndIdNot(
                    studentId, courseId, semesterId, excludeId);
        }
        return gradeDetailRepo.existsByStudentIdAndCourseIdAndSemesterId(studentId, courseId, semesterId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByGradeDetailIdAndGradeIndex(Integer gradeDetailId, Integer gradeIndex,
                                                      Integer currentExtraGradeId) {
        return gradeDetailRepo.existsByGradeDetailAndGradeIndex(
                gradeDetailId, gradeIndex, currentExtraGradeId);
    }

    @Override
    @Transactional(readOnly = true)
    public TranscriptDTO getTranscriptForClassroom(Integer classroomId, Map<String, String> params) {
        Classroom classroom = classroomService.getClassroomById(classroomId);
        if (classroom == null) {
            throw new EntityNotFoundException("Lớp không tồn tại");
        }
        List<Student> students = classroomService.getStudentsInClassroom(classroomId, params);
        TranscriptDTO dto = new TranscriptDTO();
        dto.setClassroomName(classroom.getName());
        dto.setAcademicTerm(classroom.getSemester().getAcademicYear().getYear() + " - " +
                classroom.getSemester().getSemesterType());
        dto.setCourseName(classroom.getCourse().getName());
        dto.setLecturerName(classroom.getLecturer().getLastName() + " " + classroom.getLecturer().getFirstName());
        dto.setGradeStatus(classroom.getGradeStatus());
        List<GradeDTO> list = new ArrayList<>();
        for (Student s : students) {
            GradeDTO g = new GradeDTO();
            g.setStudentId(s.getId());
            g.setStudentCode(s.getCode());
            g.setFullName(s.getUser().getLastName() + " " + s.getUser().getFirstName());
            Map<String, Integer> p = Map.of("studentId", s.getId(), "classroomId", classroomId);
            List<GradeDetail> gdList = getGradeDetail(p);
            if (!gdList.isEmpty()) {
                GradeDetail gd0 = gdList.get(0);
                g.setMidtermGrade(gd0.getMidtermGrade());
                g.setFinalGrade(gd0.getFinalGrade());
                List<Double> extras = Optional.ofNullable(gd0.getExtraGradeSet()).orElse(Collections.emptySet())
                        .stream()
                        .sorted(Comparator.comparingInt(ExtraGrade::getGradeIndex))
                        .map(ExtraGrade::getGrade)
                        .collect(Collectors.toList());
                g.setExtraGrades(extras);
            }
            list.add(g);
        }
        dto.setStudents(list);
        return dto;
    }

    @Override
    public void updateGradesForClassroom(Integer classroomId, List<GradeDTO> gradeRequests) {
        if (gradeRequests == null) return;
        int max = gradeRequests.stream()
                .mapToInt(r -> r.getExtraGrades() == null ? 0 : r.getExtraGrades().size()).max().orElse(0);
        if (max > MAX_EXTRA_GRADES) {
            throw new IllegalArgumentException("Quá số lượng điểm bổ sung");
        }
        Set<Student> all = classroomService.getClassroomWithStudents(classroomId).getStudentSet();
        // cập nhật từng yêu cầu
        for (GradeDTO dto : gradeRequests) {
            List<Double> ex = new ArrayList<>(Optional.ofNullable(dto.getExtraGrades()).orElse(Collections.emptyList()));
            while (ex.size() < max) ex.add(null);
            saveGradesForStudent(dto.getStudentId(), classroomId,
                    checkValidGrade(dto.getMidtermGrade()), checkValidGrade(dto.getFinalGrade()), ex);
        }
        // sinh viên không có trong requests -> tạo record rỗng (để đảm bảo số cột giống nhau)
        for (Student s : all) {
            boolean found = gradeRequests.stream()
                    .anyMatch(r -> r.getStudentId().equals(s.getId()));
            if (!found) {
                saveGradesForStudent(s.getId(), classroomId, null, null,
                        new ArrayList<>(Collections.nCopies(max, null)));
            }
        }
    }

    @Override
    public void uploadGradesFromCsv(Integer classroomId, MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            reader.readLine(); // skip header
            List<GradeDTO> reqs = new ArrayList<>();
            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String[] tokens = line.split(",");
                if (tokens.length < 3) {
                    throw new IllegalArgumentException("Dòng " + lineNo + " thiếu dữ liệu");
                }
                int sid = Integer.parseInt(tokens[0].trim());
                if (!classroomService.existUserInClassroom(sid, classroomId)) {
                    throw new IllegalArgumentException("SV không thuộc lớp: " + sid);
                }
                Double mid = parseDoubleSafe(tokens[1]);
                Double fin = parseDoubleSafe(tokens[2]);
                List<Double> ex = new ArrayList<>();
                for (int i = 3; i < tokens.length; i++) ex.add(parseDoubleSafe(tokens[i]));
                if (ex.size() > MAX_EXTRA_GRADES) {
                    throw new IllegalArgumentException("Dòng " + lineNo + " quá nhiều điểm bổ sung");
                }
                reqs.add(new GradeDTO(sid, mid, fin, ex));
            }
            updateGradesForClassroom(classroomId, reqs);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeDTO> getGradesByClassroom(Integer classroomId) {
        Set<Student> students = classroomService.getClassroomWithStudents(classroomId).getStudentSet();
        List<GradeDTO> result = new ArrayList<>();
        for (Student s : students) {
            GradeDTO dto = new GradeDTO();
            dto.setStudentId(s.getId());
            dto.setStudentCode(s.getCode());
            dto.setFullName(s.getUser().getLastName() + " " + s.getUser().getFirstName());
            Map<String, Integer> p = Map.of("studentId", s.getId(), "classroomId", classroomId);
            GradeDetail gd = getGradeDetail(p).stream().findFirst().orElse(null);
            if (gd != null) {
                dto.setMidtermGrade(gd.getMidtermGrade());
                dto.setFinalGrade(gd.getFinalGrade());
                List<Double> ex = Optional.ofNullable(gd.getExtraGradeSet()).orElse(Collections.emptySet())
                        .stream()
                        .sorted(Comparator.comparingInt(ExtraGrade::getGradeIndex))
                        .map(ExtraGrade::getGrade)
                        .collect(Collectors.toList());
                dto.setExtraGrades(ex);
            }
            result.add(dto);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeDetailDTO> getGradesByStudent(Integer userId, Map<String, String> params) {
        String kw = params != null ? params.get("kw") : null;
        return gradeDetailRepo.findGradeDetailsByStudentId(userId, kw);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeDetail> getGradeDetailsBySemester(Integer semesterId) {
        return gradeDetailRepo.findAllBySemesterId(semesterId);
    }

    @Override
    @Transactional(readOnly = true)
    public SemesterAnalysisResult analyzeSemester(List<GradeDetail> gradeDetails) {
        // giữ nguyên logic clustering
        List<GradeDTO> gradeList = gradeDetails.stream().map(g -> {
            GradeDTO d = new GradeDTO();
            d.setStudentId(g.getStudent().getId());
            d.setStudentCode(g.getStudent().getCode());
            d.setFullName(g.getStudent().getUser().getLastName() + " " + g.getStudent().getUser().getFirstName());
            d.setMidtermGrade(g.getMidtermGrade());
            d.setFinalGrade(g.getFinalGrade());
            List<Double> ex = Optional.ofNullable(g.getExtraGradeSet()).orElse(Collections.emptySet())
                    .stream()
                    .sorted(Comparator.comparingInt(ExtraGrade::getGradeIndex))
                    .map(ExtraGrade::getGrade).collect(Collectors.toList());
            d.setExtraGrades(ex);
            return d;
        }).toList();
        if (gradeList.isEmpty()) return new SemesterAnalysisResult();
        int maxExtra = gradeList.stream().mapToInt(d -> d.getExtraGrades() == null ? 0 : d.getExtraGrades().size()).max().orElse(0);
        double[][] data = gradeList.stream().map(d -> {
            List<Double> features = new ArrayList<>();
            features.add(Optional.ofNullable(d.getMidtermGrade()).orElse(0.0));
            features.add(Optional.ofNullable(d.getFinalGrade()).orElse(0.0));
            for (int i = 0; i < maxExtra; i++) {
                features.add(i < d.getExtraGrades().size() ? Optional.ofNullable(d.getExtraGrades().get(i)).orElse(0.0) : 0.0);
            }
            return features.stream().mapToDouble(Double::doubleValue).toArray();
        }).toArray(double[][]::new);
        KMeans kmeans = KMeans.fit(data, 2);
        int[] labels = kmeans.y;
        // tính trung bình cluster
        Map<Integer, List<double[]>> clusters = new HashMap<>();
        for (int i = 0; i < labels.length; i++) {
            clusters.computeIfAbsent(labels[i], k -> new ArrayList<>()).add(data[i]);
        }
        Map<Integer, Double> avg = clusters.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> {
                            double sum = Arrays.stream(e.getValue().stream()
                                    .flatMapToDouble(Arrays::stream).toArray()).sum();
                            return sum / (e.getValue().size() * e.getValue().get(0).length);
                        }));
        int weakCluster = avg.getOrDefault(0, 0.0) <= avg.getOrDefault(1, 0.0) ? 0 : 1;
        List<GradeClusterResultDTO> clustersDto = new ArrayList<>();
        for (int i = 0; i < gradeList.size(); i++) {
            GradeClusterResultDTO cr = new GradeClusterResultDTO();
            GradeDTO gd = gradeList.get(i);
            cr.setStudentId(gd.getStudentId());
            cr.setStudentCode(gd.getStudentCode());
            cr.setFullName(gd.getFullName());
            cr.setCourseName(gradeDetails.get(i).getCourse().getName());
            cr.setCluster(labels[i] == weakCluster ? 0 : 1);
            clustersDto.add(cr);
        }
        return buildSemesterAnalysis(clustersDto);
    }

    @Override
    public List<GradeDetail> getGradeDetailsByLecturerAndSemester(Integer lecturerId, Integer semesterId) {
        return gradeDetailRepo.findByLecturerAndSemester(lecturerId, semesterId);
    }

    @Override
    public void initGradeDetailsForClassroom(Classroom classroom) {
        Integer cid = classroom.getId();
        Set<Student> students = classroom.getStudentSet();
        int maxExtra = getGradeDetail(Map.of("classroomId", cid)).stream()
                .mapToInt(g -> g.getExtraGradeSet() == null ? 0 : g.getExtraGradeSet().size()).max().orElse(0);
        for (Student s : students) {
            Map<String, Integer> p = Map.of("classroomId", cid, "studentId", s.getId());
            if (getGradeDetail(p).isEmpty()) {
                GradeDetail gd = new GradeDetail();
                gd.setStudent(s);
                gd.setCourse(classroom.getCourse());
                gd.setSemester(classroom.getSemester());
                Set<ExtraGrade> exs = new HashSet<>();
                for (int i = 0; i < maxExtra; i++) {
                    ExtraGrade eg = new ExtraGrade();
                    eg.setGradeIndex(i);
                    exs.add(eg);
                }
                gd.setExtraGradeSet(exs);
                saveGradeDetail(gd);
            }
        }
    }

    // Helpers
    private Double checkValidGrade(Double grade) {
        if (grade != null && (grade < 0 || grade > 10)) {
            throw new IllegalArgumentException("Điểm phải từ 0 đến 10");
        }
        return grade;
    }

    private Double parseDoubleSafe(String v) {
        if (v == null || v.trim().isEmpty()) return null;
        try {
            double d = Double.parseDouble(v.trim());
            if (d < 0 || d > 10) throw new IllegalArgumentException();
            return d;
        } catch (Exception e) {
            throw new IllegalArgumentException("Giá trị điểm không hợp lệ");
        }
    }

    private SemesterAnalysisResult buildSemesterAnalysis(List<GradeClusterResultDTO> clusterResults) {
        int total = clusterResults.size();
        List<GradeClusterResultDTO> weak = clusterResults.stream().filter(r -> r.getCluster() == 0).collect(Collectors.toList());
        double ratio = total == 0 ? 0 : (weak.size() * 100.0 / total);
        SemesterAnalysisResult r = new SemesterAnalysisResult();
        r.setTotalStudents(total);
        r.setWeakStudents(weak.size());
        r.setWeakRatio(ratio);
        r.setWeakStudentList(weak);
        Map<String, List<GradeClusterResultDTO>> byCourse = clusterResults.stream().collect(Collectors.groupingBy(GradeClusterResultDTO::getCourseName));
        Map<String, Double> ratios = new HashMap<>();
        List<String> critical = new ArrayList<>();
        byCourse.forEach((course, list) -> {
            long cnt = list.stream().filter(r2 -> r2.getCluster() == 0).count();
            double cr = list.isEmpty() ? 0 : (cnt * 100.0 / list.size());
            ratios.put(course, cr);
            if (cr >= 40.0) critical.add(course);
        });
        r.setCourseWeakRatios(ratios);
        r.setCriticalCourses(critical);
        return r;
    }
}
