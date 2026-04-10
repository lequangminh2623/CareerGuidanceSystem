package com.lqm.academic_service.mappers;

import com.lqm.academic_service.dtos.SectionRequestDTO;
import com.lqm.academic_service.dtos.SectionResponseDTO;
import com.lqm.academic_service.models.Section;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface SectionMapper {

    @Mapping(target = "classroomId", source = "classroom.id")
    @Mapping(target = "classroomName", source = "classroom.name")
    @Mapping(target = "subjectName", source = "curriculum.subject.name")
    @Mapping(target = "gradeName", source = "classroom.grade.name.gradeName")
    @Mapping(target = "yearName", source = "classroom.grade.year.name")
    @Mapping(target = "semesterName", source = "curriculum.semester.name.semesterName")
    @Mapping(target = "teacherName", source = "teacherId")
    @Mapping(target = "scoreStatus", source = "scoreStatus.scoreStatusName")
    SectionResponseDTO toSectionResponseDTO(Section section, @Context Map<UUID, String> teacherMap);

    default String mapTeacherName(UUID teacherId, @Context Map<UUID, String> teacherMap) {
        if (teacherId == null || teacherMap == null) {
            return null;
        }
        return teacherMap.get(teacherId);
    }

    @Mapping(target = "classroomId", source = "classroom.id")
    @Mapping(target = "curriculumId", source = "curriculum.id")
    @Mapping(target = "scoreStatus", source = "scoreStatus.scoreStatusName")
    SectionRequestDTO toSectionRequestDTO(Section section);

    @Mapping(target = "scoreStatus", expression = "java(com.lqm.academic_service.models.ScoreStatusType.fromScoreStatusName(sectionRequestDTO.scoreStatus()))")
    @Mapping(target = "classroom", ignore = true)
    @Mapping(target = "curriculum", ignore = true)
    Section toEntity(SectionRequestDTO sectionRequestDTO);
}
