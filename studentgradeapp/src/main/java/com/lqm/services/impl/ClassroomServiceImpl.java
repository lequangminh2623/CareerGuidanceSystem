package com.lqm.services.impl;

import com.lqm.models.Classroom;
import com.lqm.models.GradeDetail;
import com.lqm.models.Student;
import com.lqm.models.User;
import com.lqm.dtos.GradeDTO;
import com.lqm.repositories.ClassroomRepository;
import com.lqm.services.ClassroomService;
import com.lqm.services.GradeDetailService;
import com.lqm.specifications.ClassroomSpecification;
import com.lqm.utils.MailUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

@Service
public class ClassroomServiceImpl implements ClassroomService {

    @Autowired
    private ClassroomRepository classroomRepo;

    @Autowired
    private GradeDetailService gradeDetailService;

    @Autowired
    private MailUtils mailUtils;

    @Override
    public Page<Classroom> getClassrooms(Map<String, String> params, Pageable pageable) {
        Specification<Classroom> spec = ClassroomSpecification.filterByParams(params);
        return classroomRepo.findAll(spec, pageable);
    }

    @Override
    public Classroom saveClassroom(Classroom classroom) {

        return classroomRepo.save(classroom);
    }

    @Override
    public Classroom getClassroomById(Integer id) {
        return classroomRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Classroom not found: " + id));
    }

    @Override
    public void deleteClassroom(Integer id) {
        Classroom cls = classroomRepo.findWithStudentsById(id);
        if (!cls.getStudentSet().isEmpty()) {
            throw new IllegalStateException("Cannot delete classroom with enrolled students.");
        }
        classroomRepo.deleteById(id);
    }

    @Override
    public Classroom getClassroomWithStudents(Integer id) {
        return classroomRepo.findWithStudentsById(id);
    }

    @Override
    public void removeStudentFromClassroom(Integer classroomId, Integer studentId) {
        Classroom cls = getClassroomWithStudents(classroomId);
        Set<Student> students = cls.getStudentSet();
        students.removeIf(s -> s.getId() == studentId);
        classroomRepo.save(cls);
    }

    @Override
    public boolean existsDuplicateClassroom(String name, Integer semesterId, Integer courseId, Integer excludeId) {
        if (excludeId == null) {
            return classroomRepo.existsByNameAndSemesterIdAndCourseId(name, semesterId, courseId);
        }
        return classroomRepo.existsByNameAndSemesterIdAndCourseIdAndIdNot(name, semesterId, courseId, excludeId);
    }

    @Override
    public boolean existsStudentInOtherClassroom(Integer studentId, Integer semesterId, Integer courseId, Integer excludeClassroomId) {
        if (excludeClassroomId == null) {
            return classroomRepo.existsByStudentSet_IdAndSemester_IdAndCourse_Id(studentId, semesterId, courseId);
        }
        return classroomRepo.existsByStudentSet_IdAndSemester_IdAndCourse_IdAndIdNot(
                studentId, semesterId, courseId, excludeClassroomId);
    }

    @Override
    public boolean existUserInClassroom(Integer userId, Integer classRoomId) {
        return classroomRepo.existsByStudentSet_User_IdAndId(userId, classRoomId);
    }

    @Override
    public Page<Classroom> getClassroomsByUser(User user, Map<String, String> params, Pageable pageable) {
        Specification<Classroom> spec = ClassroomSpecification.filterByParamsAndUser(user, params);
        return classroomRepo.findAll(spec, pageable);
    }

    @Override
    public Classroom getClassroomByForumPostId(int id) {
        return classroomRepo.findByForumPostId(id);
    }

    @Override
    public boolean lockClassroomGrades(Integer classroomId) {
        Classroom classroom = getClassroomWithStudents(classroomId);
        Set<Student> students = classroom.getStudentSet();

        for (Student student : students) {
            List<GradeDetail> details = gradeDetailService.getGradeDetail(Map.of(
                    "classroomId", classroomId,
                    "studentId", student.getId()
            ));
            if (details == null || details.isEmpty()) {
                return true;
            }
            GradeDetail gd = details.getFirst();
            if (gd.getMidtermGrade() == null || gd.getFinalGrade() == null
                    || (gd.getExtraGradeSet() != null && gd.getExtraGradeSet().stream().anyMatch(e -> e.getGrade() == null))) {
                return false;
            }
        }

        classroom.setGradeStatus("LOCKED");
        saveClassroom(classroom);

        for (Student student : classroom.getStudentSet()) {
            if (student.getUser() != null && student.getUser().getEmail() != null) {
                String subject = "Thông báo khóa điểm lớp " + classroom.getName();
                String body = String.format("Chào %s %s,\n\nBảng điểm lớp %s đã được công bố.",
                        student.getUser().getLastName(), student.getUser().getFirstName(), classroom.getName());
                mailUtils.sendEmailAsync(student.getUser().getEmail(), subject, body);
            }
        }
        return true;
    }

    @Override
    public void exportGradesToCsv(Integer classroomId, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=grades_" + classroomId + ".csv");
        List<GradeDTO> gradeList = gradeDetailService.getGradesByClassroom(classroomId);
        int maxExtra = gradeList.stream().mapToInt(g -> g.getExtraGrades() != null ? g.getExtraGrades().size() : 0).max().orElse(0);

        try (PrintWriter writer = response.getWriter()) {
            writer.print("studentId,midtermGrade,finalGrade");
            for (int i = 1; i <= maxExtra; i++) writer.print(",extra" + i);
            writer.println();
            for (GradeDTO grade : gradeList) {
                writer.print(grade.getStudentId() + "," + grade.getMidtermGrade() + "," + grade.getFinalGrade());
                List<Double> extras = grade.getExtraGrades() != null ? grade.getExtraGrades() : new ArrayList<>();
                for (int i = 0; i < maxExtra; i++) {
                    writer.print(i < extras.size() ? "," + extras.get(i) : ",");
                }
                writer.println();
            }
        }
    }

    @Override
    public boolean checkLecturerPermission(Integer classroomId) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Classroom c = getClassroomById(classroomId);
        return c.getLecturer().getEmail().equalsIgnoreCase(email);
    }

    @Override
    public boolean isLockedClassroom(Integer classroomId) {
        return "LOCKED".equalsIgnoreCase(getClassroomById(classroomId).getGradeStatus());
    }

    @Override
    public List<Student> getStudentsInClassroom(Integer classroomId, Map<String, String> params) {
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page")) - 1 : 0;
        int size = params.containsKey("size") ? Integer.parseInt(params.get("size")) : 20;
        return classroomRepo.findStudentsInClassroom(classroomId, PageRequest.of(page, size)).getContent();
    }

    @Override
    public void exportGradesToPdf(Integer classroomId, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String filename = "grades_classroom_" + classroomId + ".pdf";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        List<GradeDTO> gradeList = gradeDetailService.getGradesByClassroom(classroomId);
        int maxExtra = gradeList.stream()
                .mapToInt(g -> g.getExtraGrades() != null ? g.getExtraGrades().size() : 0)
                .max().orElse(0);

        Classroom classroom = this.getClassroomById(classroomId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);

        try (Document document = new Document(pdfDoc)) {
            PdfFont font = PdfFontFactory.createFont("static/fonts/times.ttf", PdfEncodings.IDENTITY_H);
            document.setFont(font);

            document.add(new Paragraph("BẢNG ĐIỂM")
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18));

            document.add(new Paragraph("\n"));

            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            infoTable.setMarginBottom(10);

            infoTable.addCell(new Cell().add(new Paragraph("Môn: " + classroom.getCourse().getName()).setFontSize(12)).setBorder(Border.NO_BORDER));
            infoTable.addCell(new Cell().add(new Paragraph("Lớp: " + classroom.getName()).setFontSize(12)).setBorder(Border.NO_BORDER));

            String lecturerName = classroom.getLecturer().getLastName() + " " + classroom.getLecturer().getFirstName();
            infoTable.addCell(new Cell().add(new Paragraph("Giảng viên: " + lecturerName).setFontSize(12)).setBorder(Border.NO_BORDER));
            infoTable.addCell(new Cell().add(new Paragraph("Học kỳ: " + classroom.getSemester().getSemesterType()
                    + " " + classroom.getSemester().getAcademicYear().getYear()).setFontSize(12)).setBorder(Border.NO_BORDER));

            document.add(infoTable);

            List<Float> columnWidths = new ArrayList<>();
            columnWidths.add(30f); // STT
            columnWidths.add(150f); // Họ và tên
            for (int i = 0; i < maxExtra; i++) {
                columnWidths.add(60f); // Extra points
            }
            columnWidths.add(60f); // Midterm
            columnWidths.add(60f); // Final

            Table table = new Table(columnWidths.stream().map(UnitValue::createPointValue).toArray(UnitValue[]::new));
            table.setWidth(UnitValue.createPercentValue(100));

            // Header
            table.addHeaderCell(new Cell().add(new Paragraph("STT").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Họ và tên").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            for (int i = 1; i <= maxExtra; i++) {
                table.addHeaderCell(new Cell().add(new Paragraph("Bổ sung " + i).setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            }
            table.addHeaderCell(new Cell().add(new Paragraph("Giữa kỳ").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Cuối kỳ").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));

            // Nội dung bảng
            int stt = 1;
            for (GradeDTO grade : gradeList) {
                table.addCell(new Paragraph(String.valueOf(stt++)));
                table.addCell(new Paragraph(grade.getFullName() != null ? grade.getFullName() : ""));

                List<Double> extras = grade.getExtraGrades() != null ? grade.getExtraGrades() : new ArrayList<>();
                for (int i = 0; i < maxExtra; i++) {
                    String extraValue = (i < extras.size() && extras.get(i) != null) ? extras.get(i).toString() : "";
                    table.addCell(new Paragraph(extraValue));
                }

                table.addCell(new Paragraph(grade.getMidtermGrade() != null ? grade.getMidtermGrade().toString() : ""));
                table.addCell(new Paragraph(grade.getFinalGrade() != null ? grade.getFinalGrade().toString() : ""));
            }

            document.add(table);
        }

        response.getOutputStream().write(baos.toByteArray());
        response.getOutputStream().flush();
    }
}