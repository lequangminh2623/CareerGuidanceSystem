package com.lqm.academic_service.validators;

import com.lqm.academic_service.dtos.SectionListRequest;
import com.lqm.academic_service.dtos.SectionRequestDTO;
import com.lqm.academic_service.services.SectionService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SectionListRequestValidator implements Validator, SupportsClass {

    private final SectionService sectionService;

    @Override
    public Class<?> getSupportedClass() {
        return SectionListRequest.class;
    }

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return SectionListRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        SectionListRequest request = (SectionListRequest) target;
        List<SectionRequestDTO> sections = request.sections();

        if (sections == null || sections.isEmpty()) return;

        UUID sharedClassroomId = sections.get(0).classroomId();
        if (sharedClassroomId == null) return;

        Set<UUID> incomingCurriculumIds = sections.stream()
                .map(SectionRequestDTO::curriculumId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Map chứa: Key = curriculumId đã có, Value = id của bản ghi Section chứa nó
        Map<UUID, UUID> existingDbMap = sectionService.getExistingCurriculumMap(sharedClassroomId, incomingCurriculumIds);

        Set<UUID> seenInRequest = new HashSet<>();

        for (int i = 0; i < sections.size(); i++) {
            var section = sections.get(i);
            UUID curriculumId = section.curriculumId();
            UUID currentSectionId = section.id(); // ID của row gửi lên (sẽ là null nếu là dòng mới thêm)

            if (curriculumId == null) continue;

            // 1. Check trùng lặp ngay trong List gửi lên
            if (!seenInRequest.add(curriculumId)) {
                errors.rejectValue("sections[" + i + "].curriculumId", "duplicate.in.request",
                        "Chương trình học này bị lặp lại trong danh sách.");
                continue;
            }

            // 2. Check trùng lặp với Database (Đã loại trừ Update)
            if (existingDbMap.containsKey(curriculumId)) {
                UUID existingSectionId = existingDbMap.get(curriculumId);
                if (currentSectionId == null || !currentSectionId.equals(existingSectionId)) {
                    errors.rejectValue("sections[" + i + "].curriculumId", "duplicate.in.db",
                            "Chương trình học này đã được gán cho lớp học.");
                }
            }
        }
    }
}