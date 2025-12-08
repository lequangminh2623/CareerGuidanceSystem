package com.lqm.academic_service.mappers;

import com.lqm.academic_service.dtos.ClassroomRequestDTO;
import com.lqm.academic_service.dtos.ClassroomResponseDTO;
import com.lqm.academic_service.models.Classroom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClassroomMapper {

    @Mapping(target = "gradeId", source = "classroom.grade.id")
    @Mapping(target = "studentIds", expression = "java(classroom.getStudentClassroomSet().stream()\n" +
            "                .map(com.lqm.academic_service.models.StudentClassroom::getStudentId).toList())")
    ClassroomRequestDTO toClassroomRequestDTO(Classroom classroom);

    @Mapping(target = "gradeName",
            expression = "java(classroom.getGrade().getName().getGradeName())")
    @Mapping(target = "yearName", source = "grade.year.name")
    ClassroomResponseDTO toClassroomResponseDTO(Classroom classroom);

    @Mapping(target = "grade", ignore = true)
    @Mapping(target = "studentClassroomSet", ignore = true)
    void updateEntity(@MappingTarget Classroom classroomFromDB, ClassroomRequestDTO newClassroom);

}
