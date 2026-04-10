package com.lqm.academic_service.mappers;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.ClassroomDetailsResponseDTO;
import com.lqm.academic_service.dtos.ClassroomRequestDTO;
import com.lqm.academic_service.dtos.ClassroomResponseDTO;
import com.lqm.academic_service.models.Classroom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ClassroomMapper {

    @Mapping(target = "gradeId", source = "grade.id")
    @Mapping(target = "studentIds",
            expression = """
                java(classroom.getStudentClassroomSet().stream()
                .map(com.lqm.academic_service.models.StudentClassroom::getStudentId).toList())
            """)
    ClassroomRequestDTO toClassroomRequestDTO(Classroom classroom);

    ClassroomResponseDTO toClassroomResponseDTO(Classroom classroom);

    @Mapping(target = "name", source = "classroom")
    AcademicResponseDTO toClassroomDetailNameDTO(Classroom classroom);

    default String mapFullClassName(Classroom classroom) {
        if (classroom == null) return null;

        return String.format("%s - %s (%s)",
                classroom.getName(),
                classroom.getGrade().getName().getGradeName(),
                classroom.getGrade().getYear().getName());
    }

    @Mapping(target = "grade", ignore = true)
    @Mapping(target = "studentClassroomSet", ignore = true)
    void updateEntity(@MappingTarget Classroom classroomFromDB, ClassroomRequestDTO newClassroom);

    @Mapping(target = "gradeName", source = "grade.name.gradeName")
    @Mapping(target = "studentIds",
            expression = """
                java(classroom.getStudentClassroomSet().stream()
                .map(com.lqm.academic_service.models.StudentClassroom::getStudentId).toList())
            """)
    ClassroomDetailsResponseDTO toClassroomDetailsResponseDTO(Classroom classroom);
}
