package com.lqm.score_service.services.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.lqm.score_service.clients.ClassroomClient;
import com.lqm.score_service.clients.SectionClient;
import com.lqm.score_service.dtos.SyncScoreRequestDTO;
import com.lqm.score_service.dtos.SectionResponseDTO;
import com.lqm.score_service.dtos.UserResponseDTO;
import com.lqm.score_service.exceptions.BadRequestException;
import com.lqm.score_service.exceptions.ResourceNotFoundException;
import com.lqm.score_service.models.*;
import com.lqm.score_service.repositories.*;
import com.lqm.score_service.services.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lqm.score_service.specifications.ScoreDetailSpecification.filter;

@Service
@Transactional
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {

    @Value("${max.extraScores}")
    private int MAX_EXTRA_SCORES;
    private final SectionClient sectionClient;
    private final ScoreDetailRepository scoreDetailRepo;
    private final ClassroomClient classroomClient;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public Page<ScoreDetail> getScoreDetails(Map<String, String> params, Pageable pageable) {
        Specification<ScoreDetail> spec = filter(params != null ? params : Map.of());
        return scoreDetailRepo.findAll(spec, pageable);
    }

    @Override
    public ScoreDetail getScoreDetailByTranscriptIdAndStudentId(UUID sectionId, UUID studentId) {
        return scoreDetailRepo.findBySectionIdAndStudentId(sectionId, studentId).orElse(null);
    }

    @Override
    public void deleteScoreDetails(UUID studentId, List<UUID> transcriptIds) {
        scoreDetailRepo.deleteAllByStudentIdAndSectionIdIn(studentId, transcriptIds);
    }

    @Override
    public boolean isTranscriptFullyScored(UUID sectionId) {
        return scoreDetailRepo.countIncompleteScores(sectionId) == 0;
    }

    @Override
    public void syncScoresForClassroom(SyncScoreRequestDTO request) {

        // 1. THỰC HIỆN XÓA (Clean up dữ liệu cũ trước)
        if (request.removedStudentIds() != null && !request.removedStudentIds().isEmpty()
                && request.sectionIds() != null && !request.sectionIds().isEmpty()) {

            scoreDetailRepo.deleteAllBySectionIdInAndStudentIdIn(
                    request.sectionIds(),
                    request.removedStudentIds());
        }

        // 2. THỰC HIỆN THÊM (Khởi tạo điểm cho học sinh mới)
        if (request.newStudentIds() != null && !request.newStudentIds().isEmpty()
                && request.sectionIds() != null && !request.sectionIds().isEmpty()) {

            List<ScoreDetail> toSave = new ArrayList<>();

            for (UUID sectionId : request.sectionIds()) {
                // Lấy số lượng cột extraScore lớn nhất hiện hành trong Section này
                ScoreDetail sample = scoreDetailRepo.findFirstBySectionId(sectionId);
                int currentMaxExtraScores = (sample != null && sample.getExtraScoreSet() != null)
                        ? sample.getExtraScoreSet().size()
                        : 0;

                for (UUID studentId : request.newStudentIds()) {
                    ScoreDetail scoreDetail = ScoreDetail.builder()
                            .sectionId(sectionId)
                            .studentId(studentId)
                            .build();

                    // Dùng LinkedHashSet thay vì HashSet để giữ đúng thứ tự của cột điểm (index 0,
                    // 1, 2)
                    Set<ExtraScore> extraScores = new LinkedHashSet<>();

                    // Tạo từng ExtraScore, đánh index từ 0 đến currentMaxExtraScores - 1
                    for (int i = 0; i < currentMaxExtraScores; i++) {
                        ExtraScore extraScore = ExtraScore.builder()
                                .scoreIndex(i)
                                .score(null)
                                .scoreDetail(scoreDetail)
                                .build();

                        extraScores.add(extraScore);
                    }
                    scoreDetail.setExtraScoreSet(extraScores);

                    toSave.add(scoreDetail);
                }
            }

            if (!toSave.isEmpty()) {
                scoreDetailRepo.saveAll(toSave);
            }
        }
    }

    @Override
    public List<ScoreDetail> saveScores(List<ScoreDetail> scoreRequests) {
        UUID sectionId = scoreRequests.getFirst().getSectionId();

        // 1. Validate Students & Max Columns
        SectionResponseDTO sectionResponseDTO = sectionClient.getSectionResponseById(sectionId);

        List<UUID> requestStudentIds = scoreRequests.stream().map(ScoreDetail::getStudentId).toList();
        List<UUID> invalidStudentIds = classroomClient.getNonExistingStudentIds(sectionResponseDTO.classroomId(),
                requestStudentIds);
        if (invalidStudentIds != null && !invalidStudentIds.isEmpty()) {
            throw new ResourceNotFoundException(
                    messageSource.getMessage("classroom.student.notIn", null, Locale.getDefault())
                            + " " + invalidStudentIds.getFirst());
        }

        int max = calculateMaxColumns(scoreRequests);
        if (max > MAX_EXTRA_SCORES) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("extraScore.exceed", null, Locale.getDefault())
                            + " " + MAX_EXTRA_SCORES);
        }

        // 2. Chuyển Request thành Map
        Map<UUID, ScoreDetail> requestMap = scoreRequests.stream().collect(Collectors.toMap(
                ScoreDetail::getStudentId, Function.identity(), (a, b) -> a));

        Map<UUID, ScoreDetail> dbMap = scoreDetailRepo.findBySectionId(sectionId)
                .stream()
                .collect(Collectors.toMap(ScoreDetail::getStudentId, Function.identity()));

        // 4. Lấy danh sách học sinh để map
        List<UUID> allStudentIds = classroomClient.getClassroomDetailsResponseById(sectionResponseDTO.classroomId())
                .studentIds();
        List<ScoreDetail> toSave = new ArrayList<>();

        for (UUID studentId : allStudentIds) {
            // Nếu DB chưa có thì mới tạo mới bằng Builder
            ScoreDetail dbScoreEntity = dbMap.getOrDefault(
                    studentId,
                    ScoreDetail.builder().sectionId(sectionId).studentId(studentId).build());

            // Lấy dữ liệu mới từ Form gửi lên
            ScoreDetail newRequestData = requestMap.get(studentId);

            // NẾU form có gửi điểm của học sinh này lên -> Đè điểm mới vào dbScoreEntity
            if (newRequestData != null) {
                dbScoreEntity.setMidtermScore(newRequestData.getMidtermScore());
                dbScoreEntity.setFinalScore(newRequestData.getFinalScore());

                // Hàm setExtraScoreSet sẽ tự động xử lý thêm/xóa/sửa các cột điểm phụ an toàn
                dbScoreEntity.setExtraScoreSet(newRequestData.getExtraScoreSet());
            }

            dbScoreEntity.setUpdatedDate(LocalDateTime.now());

            // Cân bằng số lượng cột
            mergeExtraScores(dbScoreEntity, max);

            toSave.add(dbScoreEntity);
        }
        return scoreDetailRepo.saveAll(toSave);
    }

    /**
     * Helper to:
     * Calculate the max quantity of an extra score list and check exceed quantity
     * limit.
     */
    private int calculateMaxColumns(List<ScoreDetail> scoreRequests) {
        int max = 0;
        for (ScoreDetail r : scoreRequests) {
            if (r.getExtraScoreSet() != null) {
                max = Math.max(max, r.getExtraScoreSet().size());
            }
        }
        return max;
    }

    /**
     * Helper to sync the List<Double> values into the Set<ExtraScore> entities.
     * It reuses existing entities by index to avoid deleting/re-inserting.
     **/
    private void mergeExtraScores(ScoreDetail sd, int maxCount) {
        // Map existing ExtraScores by their index: Index -> Entity
        Map<Integer, ExtraScore> existingMap = Optional.ofNullable(sd.getExtraScoreSet()).orElse(Set.of())
                .stream().collect(Collectors.toMap(ExtraScore::getScoreIndex, Function.identity()));

        Set<ExtraScore> finalSet = new LinkedHashSet<>();

        for (int i = 0; i < maxCount; i++) {
            ExtraScore es = existingMap.getOrDefault(i, ExtraScore.builder().scoreIndex(i).build());
            es.setScoreDetail(sd);

            finalSet.add(es);
        }

        sd.setExtraScoreSet(finalSet);
    }

    @Override
    public void importScoresFromCsv(UUID sectionId, MultipartFile file) throws IOException {
        SectionResponseDTO sectionResponseDTO = sectionClient.getSectionResponseById(sectionId);

        Map<String, UserResponseDTO> userMap = classroomClient.getStudentsInClassroom(
                sectionResponseDTO.classroomId(), Map.of()).getContent()
                .stream()
                .collect(Collectors.toMap(UserResponseDTO::code, Function.identity()));

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .get();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser parser = CSVParser.parse(reader, format)) {

            List<ScoreDetail> reqs = new ArrayList<>();
            boolean pastHeaders = false;

            for (CSVRecord record : parser) {
                if (!pastHeaders) {
                    if (record.size() > 0 && record.get(0).toLowerCase().contains("mã học sinh")) {
                        pastHeaders = true;
                    }
                    continue;
                }

                long lineNo = record.getRecordNumber() + 1;

                try {
                    String code = record.size() > 0 ? record.get(0) : null;
                    if (code == null || code.isBlank())
                        throw new IllegalArgumentException(
                                messageSource.getMessage("student.code.missing", null, Locale.getDefault()));
                    UUID sid = userMap.get(code).id();

                    String name = record.size() > 1 ? record.get(1) : null;
                    if (!(userMap.get(code).lastName() + " " + userMap.get(code).firstName()).equals(name))
                        throw new IllegalArgumentException(
                                messageSource.getMessage("student.name.incorrect", null, Locale.getDefault()));

                    Double mid = record.size() > 2 ? parseDoubleSafe(record.get(2)) : null;
                    Double fin = record.size() > 3 ? parseDoubleSafe(record.get(3)) : null;
                    ScoreDetail sd = ScoreDetail.builder().sectionId(sectionId).studentId(sid).midtermScore(mid)
                            .finalScore(fin).build();
                    Set<ExtraScore> ex = new LinkedHashSet<>();
                    for (int i = 4; i < record.size(); i++) {
                        ex.add(ExtraScore.builder().score(parseDoubleSafe(record.get(i))).scoreIndex(i - 4)
                                .scoreDetail(sd).build());
                    }

                    if (ex.size() > MAX_EXTRA_SCORES) {
                        throw new IllegalArgumentException(
                                messageSource.getMessage("extraScore.exceed", null, Locale.getDefault())
                                        + " " + MAX_EXTRA_SCORES);
                    }
                    sd.setExtraScoreSet(ex);
                    reqs.add(sd);

                } catch (IllegalArgumentException e) {
                    throw new BadRequestException(String.format("line.error %d: %s", lineNo, e.getMessage()));
                } catch (Exception e) {
                    throw new BadRequestException(String.format("line.error %d", lineNo));
                }
            }

            if (!reqs.isEmpty()) {
                saveScores(reqs);
            }
        }
    }

    /**
     * Helper: Trả về null nếu chuỗi rỗng hoặc null.
     * Ném lỗi nếu chuỗi là số nhưng ngoài khoảng 0-10.
     **/
    private Double parseDoubleSafe(String v) {
        if (v == null || v.trim().isEmpty())
            return null;
        try {
            double d = Double.parseDouble(v.trim());
            if (d < 0 || d > 10) {
                throw new IllegalArgumentException("score.invalid " + v);
            }
            return d;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("score.invalid " + v);
        }
    }

    private byte[] cachedFontBytes;
    private byte[] cachedBoldFontBytes;

    @PostConstruct
    public void initFont() {
        try {
            ClassPathResource fontResource = new ClassPathResource("static/fonts/times.ttf");
            this.cachedFontBytes = fontResource.getInputStream().readAllBytes();
            ClassPathResource boldFontResource = new ClassPathResource("static/fonts/timesbd.ttf");
            this.cachedBoldFontBytes = boldFontResource.getInputStream().readAllBytes();
        } catch (IOException e) {
            System.err.println("Could not load custom font: " + e.getMessage());
        }
    }

    // Helper Record to hold data
    private record TranscriptData(
            SectionResponseDTO section,
            List<ScoreDetail> scoreList,
            Map<UUID, UserResponseDTO> userMap,
            int maxExtra) {
    }

    private TranscriptData prepareTranscriptData(UUID sectionId) {
        // 1. Get Section Info
        SectionResponseDTO section = sectionClient.getSectionResponseById(sectionId);

        // 2. Get Scores
        List<ScoreDetail> scoreList = this.getScoreDetails(
                Map.of("sectionId", sectionId.toString()),
                Pageable.unpaged()).getContent();

        // 3. Get Students (Ensure we fetch ALL students, unpaged)
        Map<UUID, UserResponseDTO> userMap = classroomClient.getStudentsInClassroom(
                section.classroomId(),
                Map.of("page", "0", "size", "1000") // Ensure we get everyone
        ).getContent()
                .stream()
                .collect(Collectors.toMap(UserResponseDTO::id, Function.identity()));

        int maxExtra = calculateMaxColumns(scoreList);

        return new TranscriptData(section, scoreList, userMap, maxExtra);
    }

    // --- 2. CSV EXPORT ---
    @Override
    public byte[] generateScoreCsv(UUID sectionId) {
        TranscriptData data = prepareTranscriptData(sectionId);

        List<String> headers = new ArrayList<>(List.of("Mã học sinh", "Họ và tên", "Điểm GK", "Điểm CK"));
        for (int i = 1; i <= data.maxExtra; i++)
            headers.add("Điểm TX " + i);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {

            writer.write('\ufeff'); // BOM for Excel

            CSVFormat format = CSVFormat.DEFAULT;

            try (CSVPrinter printer = new CSVPrinter(writer, format)) {

                // In metadata header
                printer.printRecord("BẢNG ĐIỂM KẾT QUẢ HỌC TẬP");
                printer.printRecord();
                printer.printRecord("Lớp học:", data.section.classroomName(), "Khối lớp:", data.section.gradeName());
                printer.printRecord("Học kỳ:", data.section.semesterName(), "Năm học:", data.section.yearName());
                printer.printRecord("Giáo viên:", data.section.teacherName(), "Môn học:", data.section.subjectName());
                printer.printRecord();

                // In dòng tiêu đề bảng điểm
                printer.printRecord(headers);

                for (ScoreDetail score : data.scoreList) {
                    UserResponseDTO student = data.userMap.get(score.getStudentId());

                    // Safe Handling if student is not found
                    String code = (student != null) ? student.code() : "UNKNOWN";
                    String fullName = formatFullName(student);

                    List<Object> record = new ArrayList<>();
                    record.add(code);
                    record.add(fullName);
                    record.add(formatScore(score.getMidtermScore()));
                    record.add(formatScore(score.getFinalScore()));

                    List<Double> extras = getExtraScores(score);
                    for (int i = 0; i < data.maxExtra; i++) {
                        record.add(i < extras.size() ? formatScore(extras.get(i)) : "");
                    }
                    printer.printRecord(record);
                }
            }
            writer.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --- 3. PDF EXPORT ---
    @Override
    public byte[] generateScorePdf(UUID sectionId) {
        TranscriptData data = prepareTranscriptData(sectionId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(20, 20, 20, 20);

            // Font Handling (Use cached bytes)
            PdfFont font;
            PdfFont boldFont;
            if (cachedFontBytes != null && cachedBoldFontBytes != null) {
                font = PdfFontFactory.createFont(cachedFontBytes, com.itextpdf.io.font.PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
                boldFont = PdfFontFactory.createFont(cachedBoldFontBytes, com.itextpdf.io.font.PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
            } else {
                // Fallback - Warning: This will break Vietnamese characters
                font = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.TIMES_ROMAN);
                boldFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.TIMES_BOLD);
            }
            document.setFont(font);

            // National Motto Header
            Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 1, 1 })).useAllAvailableWidth();

            Cell leftCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
            leftCell.add(new Paragraph("BỘ GIÁO DỤC VÀ ĐÀO TẠO").setFontSize(11));
            leftCell.add(new Paragraph("Scholar").setFont(boldFont).setFontSize(11));

            Cell rightCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
            rightCell.add(new Paragraph("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM").setFont(boldFont).setFontSize(11));
            rightCell.add(new Paragraph("Độc lập - Tự do - Hạnh phúc").setFont(boldFont).setFontSize(11));
            rightCell.add(new Paragraph("-----------------------").setFontSize(11));

            headerTable.addCell(leftCell);
            headerTable.addCell(rightCell);
            document.add(headerTable);
            document.add(new Paragraph("\n"));

            // Header Info
            document.add(new Paragraph("BẢNG ĐIỂM KẾT QUẢ HỌC TẬP")
                    .setFont(boldFont).setFontSize(17).setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

            Table infoTable = new Table(UnitValue.createPercentArray(new float[] { 1, 3, 1, 3 }))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
            addInfoRow(infoTable, "Lớp học:", data.section.classroomName(), boldFont);
            addInfoRow(infoTable, "Khối lớp:", data.section.gradeName(), boldFont);
            addInfoRow(infoTable, "Học kỳ:", data.section.semesterName(), boldFont);
            addInfoRow(infoTable, "Năm học:", data.section.yearName(), boldFont);
            addInfoRow(infoTable, "Giáo viên:", data.section.teacherName(), boldFont);
            addInfoRow(infoTable, "Môn học:", data.section.subjectName(), boldFont);
            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // Dynamic Table Columns
            // 1(STT) + 3(Code) + 6(Name) + 2(Mid) + 2(Final) + 2*Extras
            float[] colWidths = new float[5 + data.maxExtra];
            colWidths[0] = 1.2f;
            colWidths[1] = 3.5f;
            colWidths[2] = 6f;
            colWidths[3] = 2.5f;
            colWidths[4] = 2.5f;
            Arrays.fill(colWidths, 5, colWidths.length, 2.5f);

            Table table = new Table(UnitValue.createPercentArray(colWidths)).useAllAvailableWidth();

            // Table Header
            addHeaderCell(table, "STT", boldFont);
            addHeaderCell(table, "Mã SV", boldFont);
            addHeaderCell(table, "Họ và tên", boldFont);
            addHeaderCell(table, "GK", boldFont);
            addHeaderCell(table, "CK", boldFont);
            for (int i = 1; i <= data.maxExtra; i++)
                addHeaderCell(table, "TX" + i, boldFont);

            // Table Body
            int stt = 1;
            boolean isOdd = false;
            for (ScoreDetail score : data.scoreList) {
                isOdd = !isOdd;
                UserResponseDTO student = data.userMap.get(score.getStudentId());
                com.itextpdf.kernel.colors.Color bgColor = isOdd
                        ? new com.itextpdf.kernel.colors.DeviceRgb(249, 250, 251)
                        : ColorConstants.WHITE;

                addCellCenter(table, String.valueOf(stt++), bgColor);
                addCellCenter(table, student != null ? student.code() : "N/A", bgColor); // Null check

                // Fixed Name Logic: Combine Last + First
                table.addCell(new Cell().add(new Paragraph(formatFullName(student)))
                        .setTextAlignment(TextAlignment.LEFT)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setPaddingLeft(6)
                        .setPaddingTop(4)
                        .setPaddingBottom(4)
                        .setBorder(new com.itextpdf.layout.borders.SolidBorder(
                                com.itextpdf.kernel.colors.ColorConstants.GRAY, 0.5f))
                        .setBackgroundColor(bgColor));

                addCellCenter(table, formatScore(score.getMidtermScore()), bgColor);
                addCellCenter(table, formatScore(score.getFinalScore()), bgColor);

                List<Double> extras = getExtraScores(score);
                for (int i = 0; i < data.maxExtra; i++) {
                    addCellCenter(table, i < extras.size() ? formatScore(extras.get(i)) : "-", bgColor);
                }
            }

            document.add(table);

            // Footer Signature
            document.add(new Paragraph("\n"));
            Table footerTable = new Table(UnitValue.createPercentArray(new float[] { 1, 1 })).useAllAvailableWidth();
            footerTable.addCell(new Cell().setBorder(Border.NO_BORDER));

            Cell signCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
            LocalDateTime now = LocalDateTime.now();
            signCell.add(new Paragraph(String.format("Ngày %02d tháng %02d năm %d", now.getDayOfMonth(),
                    now.getMonthValue(), now.getYear())).setFontSize(11).setItalic());
            signCell.add(new Paragraph("Giáo viên phụ trách").setFont(boldFont).setFontSize(12).setMarginBottom(50));
            signCell.add(new Paragraph(data.section.teacherName() != null ? data.section.teacherName() : "")
                    .setFont(boldFont).setFontSize(12));

            footerTable.addCell(signCell);
            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ================= HELPER METHODS =================

    private void addInfoRow(Table table, String label, String value, PdfFont boldFont) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(boldFont)).setBorder(Border.NO_BORDER).setPadding(2));
        table.addCell(
                new Cell().add(new Paragraph(value != null ? value : "")).setBorder(Border.NO_BORDER).setPadding(2));
    }

    private void addHeaderCell(Table table, String text, PdfFont boldFont) {
        table.addHeaderCell(new Cell()
                .add(new Paragraph(text).setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(41, 128, 185))
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(ColorConstants.DARK_GRAY, 1.5f))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(6));
    }

    private void addCellCenter(Table table, String text, com.itextpdf.kernel.colors.Color bgColor) {
        table.addCell(new Cell()
                .add(new Paragraph(text).setFontSize(10))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBackgroundColor(bgColor)
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(com.itextpdf.kernel.colors.ColorConstants.GRAY,
                        0.5f))
                .setPaddingTop(4)
                .setPaddingBottom(4));
    }

    private List<Double> getExtraScores(ScoreDetail score) {
        return score.getExtraScoreSet() != null
                ? score.getExtraScoreSet().stream().map(ExtraScore::getScore).toList()
                : new ArrayList<>();
    }

    private String formatScore(Double score) {
        // Nếu null trả về "-", nếu có điểm thì format 1 số lẻ (vd: 8.5)
        return score == null ? "-" : String.format("%.1f", score);
    }

    private String formatFullName(UserResponseDTO student) {
        if (student == null)
            return "Unknown Student";
        String last = student.lastName() != null ? student.lastName() : "";
        String first = student.firstName() != null ? student.firstName() : "";
        return (last + " " + first).trim();
    }

}
