package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.UserMessageResponseDTO;
import com.lqm.academic_service.dtos.UserResponseDTO;
import com.lqm.academic_service.dtos.MailMessageDTO;
import com.lqm.academic_service.events.*;
import com.lqm.academic_service.exceptions.BadRequestException;
import com.lqm.academic_service.exceptions.ForbiddenException;
import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.clients.ScoreClient;
import com.lqm.academic_service.models.*;
import com.lqm.academic_service.repositories.SectionRepository;
import com.lqm.academic_service.services.*;
import com.lqm.academic_service.specifications.SectionSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepo;
    private final MessageSource messageSource;
    private final ClassroomService classroomService;
    private final CurriculumService curriculumService;
    private final UserClient userClient;
    private final ScoreClient scoreClient;
    private final AcademicEventPublisher eventPublisher;
    private final EmailPublisher emailPublisher;

    @Override
    public Page<Section> getSectionsByIds(List<UUID> ids, Map<String, String> params, Pageable pageable) {
        Specification<Section> spec = SectionSpecification.filterByParams(params)
                .and(SectionSpecification.hasIdIn(ids));
        return sectionRepo.findAll(spec, pageable);
    }

    @Override
    public Page<Section> getSections(Map<String, String> params, Pageable pageable) {
        Specification<Section> spec = SectionSpecification.filterByParams(params);
        return sectionRepo.findAll(spec, pageable);
    }

    @Override
    public Section getSectionById(UUID id) {
        return sectionRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                messageSource.getMessage("section.notFound", null, Locale.getDefault())));
    }

    @Override
    public void saveSections(Map<UUID, Section> curriculumSectionMap, UUID classroomId) {
        Classroom classroom = classroomService.getClassroomById(classroomId);

        // Track existing teachers to determine chat group updates
        List<UUID> incomingSectionIds = curriculumSectionMap.values().stream()
                .map(Section::getId)
                .filter(Objects::nonNull)
                .toList();

        Map<UUID, UUID> existingTeacherMap = new HashMap<>();
        if (!incomingSectionIds.isEmpty()) {
            sectionRepo.findAllById(incomingSectionIds).forEach(sec -> {
                existingTeacherMap.put(sec.getId(), sec.getTeacherId());
            });
        }

        Map<UUID, Curriculum> curriculumEntities = curriculumService.getCurriculumsByIds(
                curriculumSectionMap.keySet().stream().toList(),
                Map.of(),
                Pageable.unpaged()).stream().collect(Collectors.toMap(Curriculum::getId, c -> c));

        curriculumSectionMap.forEach((curriculumId, section) -> {
            Curriculum curriculum = curriculumEntities.get(curriculumId);
            section.setCurriculum(curriculum);
            section.setClassroom(classroom);
        });

        List<Section> savedSections = sectionRepo.saveAll(curriculumSectionMap.values());
        handleChatGroups(classroom, savedSections, existingTeacherMap);
    }

    @Override
    public Section saveSingleSection(Section section, UUID classroomId, UUID curriculumId) {
        Classroom classroom = classroomService.getClassroomById(classroomId);
        Curriculum curriculum = curriculumService.getCurriculumById(curriculumId);

        section.setCurriculum(curriculum);
        section.setClassroom(classroom);

        Section savedSection = sectionRepo.save(section);

        // Đồng bộ chat groups
        handleChatGroups(classroom, List.of(savedSection), Map.of());

        // Đồng bộ điểm riêng cho section mới tạo - BẤT ĐỒNG BỘ qua RabbitMQ
        List<UUID> studentIds = classroom.getStudentClassroomSet().stream()
                .map(StudentClassroom::getStudentId)
                .toList();

        if (!studentIds.isEmpty()) {
            eventPublisher.publishScoreSync(new ScoreSyncEvent(
                    List.of(savedSection.getId()),
                    studentIds,
                    Collections.emptyList()));
        }

        return savedSection;
    }

    @Override
    public void changeScoreStatus(UUID id, ScoreStatusType scoreStatusType) {
        Section section = this.getSectionById(id);
        section.setScoreStatus(scoreStatusType);
        sectionRepo.save(section);
    }

    @Override
    public void lockSection(UUID sectionId) {
        Section section = getSectionById(sectionId);

        if (ScoreStatusType.LOCKED.equals(section.getScoreStatus())) {
            throw new BadRequestException(messageSource.getMessage("transcript.locked", null, Locale.getDefault()));
        }

        if (!Boolean.TRUE.equals(scoreClient.isTranscriptFullyScored(sectionId))) {
            throw new ForbiddenException(
                    messageSource.getMessage("transcript.notFullyScored", null, Locale.getDefault()));
        }

        section.setScoreStatus(ScoreStatusType.LOCKED);
        sectionRepo.save(section);

        // Gửi email thông báo cho học sinh bất đồng bộ qua RabbitMQ
        notifyStudentsAboutLockedSection(section);
    }

    private void notifyStudentsAboutLockedSection(Section section) {
        try {
            List<UUID> studentIds = section.getClassroom().getStudentClassroomSet().stream()
                    .map(StudentClassroom::getStudentId)
                    .toList();

            if (!studentIds.isEmpty()) {
                Page<UserMessageResponseDTO> students = userClient.getUsersMessages(studentIds, Map.of("page", ""));

                String subjectName = section.getCurriculum().getSubject().getName();
                String className = section.getClassroom().getName();
                String semesterName = section.getCurriculum().getSemester().getName().toString();
                String schoolYear = section.getCurriculum().getSemester().getYear().getName();

                String emailSubject = String.format("[Scholar] Thông báo khóa điểm môn %s - Lớp %s", subjectName,
                        className);

                String bodyTemplate = """
                        Chào %s,

                        Chúng tôi xin thông báo rằng giáo viên đã thực hiện khóa bảng điểm cho:
                        - Môn học: %s
                        - Lớp: %s
                        - Học kỳ: %s
                        - Năm học: %s

                        Hiện tại, bạn đã có thể đăng nhập vào hệ thống Scholar để kiểm tra kết quả học tập của mình.

                        Nếu có bất kỳ thắc mắc nào về điểm số, vui lòng liên hệ trực tiếp với giáo viên bộ môn hoặc văn phòng nhà trường.

                        Trân trọng,
                        Đội ngũ Scholar!
                        """;

                students.forEach(student -> {
                    if (student.email() != null && !student.email().isBlank()) {
                        String fullName = student.lastName() + " " + student.firstName();
                        String body = String.format(bodyTemplate,
                                fullName,
                                subjectName,
                                className,
                                semesterName,
                                schoolYear);
                        emailPublisher.publish(new MailMessageDTO(student.email(), emailSubject, body));
                    }
                });
            }
        } catch (Exception e) {
            // Không làm gián đoạn luồng chính nếu có lỗi khi đẩy vào queue email
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSection(UUID id) {
        // Publish event BẤT ĐỒNG BỘ để xóa điểm trong score-service
        eventPublisher.publishScoreSync(new ScoreSyncEvent(
                List.of(id),
                Collections.emptyList(),
                Collections.emptyList()));

        // Publish event BẤT ĐỒNG BỘ để xóa nhóm chat
        eventPublisher.publishChatGroupDelete(new ChatGroupDeleteEvent(List.of(id)));

        sectionRepo.deleteById(id);
    }

    @Override
    public boolean isLockedSection(UUID id) {
        return ScoreStatusType.LOCKED.equals(this.getSectionById(id).getScoreStatus());
    }

    @Override
    public boolean existTeacherInSection(UUID teacherId, UUID sectionId) {
        return sectionRepo.existsByTeacherIdAndId(teacherId, sectionId);
    }

    @Override
    public boolean existSectionById(UUID sectionId) {
        return sectionRepo.existsById(sectionId);
    }

    @Override
    public Map<UUID, UUID> getExistingCurriculumMap(UUID classroomId, Set<UUID> curriculumIds) {
        if (classroomId == null || curriculumIds == null || curriculumIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> results = sectionRepo.findCurriculumAndSectionIds(classroomId, curriculumIds);

        Map<UUID, UUID> map = new HashMap<>();
        for (Object[] row : results) {
            UUID currId = (UUID) row[0];
            UUID secId = (UUID) row[1];
            map.put(currId, secId);
        }
        return map;
    }

    @Override
    public Map<UUID, String> buildTeacherMap(List<Section> sections) {

        List<UUID> teacherIds = sections.stream()
                .map(Section::getTeacherId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (teacherIds.isEmpty()) {
            return Map.of();
        }

        Page<UserResponseDTO> userPage = userClient.getUsers(teacherIds, Map.of());

        return userPage.getContent().stream()
                .collect(Collectors.toMap(
                        UserResponseDTO::id,
                        user -> user.lastName() + " " + user.firstName()));
    }

    private void handleChatGroups(Classroom classroom, List<Section> savedSections,
            Map<UUID, UUID> existingTeacherMap) {
        List<UUID> studentIds = classroom.getStudentClassroomSet().stream()
                .map(StudentClassroom::getStudentId)
                .toList();

        List<String> studentEmails = new ArrayList<>();
        if (!studentIds.isEmpty()) {
            studentEmails = userClient.getUsersMessages(studentIds, Map.of("page", "")).getContent().stream()
                    .filter(u -> u.email() != null && !u.email().isBlank())
                    .map(UserMessageResponseDTO::email)
                    .toList();
        }

        Map<UUID, String> teacherEmails = new HashMap<>();

        for (Section sec : savedSections) {
            UUID oldTeacherId = existingTeacherMap.get(sec.getId());
            UUID newTeacherId = sec.getTeacherId();

            if (Objects.equals(oldTeacherId, newTeacherId))
                continue; // No change

            if (oldTeacherId == null && newTeacherId != null) {
                // Tạo mới group chat - PUBLISH EVENT
                String newEmail = getTeacherEmail(newTeacherId, teacherEmails);
                if (newEmail != null) {
                    String groupName = classroom.getName() + " - " + sec.getCurriculum().getSubject().getName() + " - "
                            + sec.getCurriculum().getSemester().getName() + " - "
                            + sec.getCurriculum().getSemester().getYear().getName();
                    eventPublisher.publishChatGroupCreate(
                            new ChatGroupCreateEvent(sec.getId(), groupName, newEmail, studentEmails));
                }
            } else if (oldTeacherId != null && newTeacherId != null) {
                // Đổi giáo viên - PUBLISH EVENT
                String oldEmail = getTeacherEmail(oldTeacherId, teacherEmails);
                String newEmail = getTeacherEmail(newTeacherId, teacherEmails);
                if (oldEmail != null && newEmail != null) {
                    eventPublisher.publishChatGroupUpdateTeacher(
                            new ChatGroupUpdateTeacherEvent(sec.getId(), oldEmail, newEmail));
                }
            } else if (oldTeacherId != null && newTeacherId == null) {
                // Xóa group chat - PUBLISH EVENT
                eventPublisher.publishChatGroupDelete(new ChatGroupDeleteEvent(List.of(sec.getId())));
            }
        }
    }

    private String getTeacherEmail(UUID teacherId, Map<UUID, String> emailCache) {
        if (emailCache.containsKey(teacherId))
            return emailCache.get(teacherId);
        Page<UserMessageResponseDTO> users = userClient.getUsersMessages(List.of(teacherId), Map.of("page", ""));
        String email = users.getContent().stream()
                .findFirst()
                .map(UserMessageResponseDTO::email)
                .orElse(null);
        emailCache.put(teacherId, email);
        return email;
    }
}
