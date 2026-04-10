package com.lqm.academic_service.mappers;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.GradeDetailsResponseDTO;
import com.lqm.academic_service.dtos.GradeRequestDTO;
import com.lqm.academic_service.models.Grade;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GradeMapper {

    @Mapping(target = "name", source = "name.gradeName")
    AcademicResponseDTO toAcademicResponseDTO(Grade grade);

    @Mapping(target = "name", source = "name.gradeName")
    @Mapping(target = "yearName", source = "year.name")
    GradeDetailsResponseDTO toGradeDetailsResponseDTO(Grade grade);

    @Mapping(target = "name", source = "name.gradeName")
    @Mapping(target = "yearId", source = "year.id")
    GradeRequestDTO toGradeRequestDTO(Grade grade);

    @Mapping(target = "name",
            expression = "java(com.lqm.academic_service.models.GradeType.fromGradeName(gradeRequestDTO.name()))")
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "classroomSet", ignore = true)
    Grade toEntity(GradeRequestDTO gradeRequestDTO);
}
