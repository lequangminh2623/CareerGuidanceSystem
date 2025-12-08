package com.lqm.academic_service.mappers;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.SemesterRequestDTO;
import com.lqm.academic_service.models.Semester;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SemesterMapper {
    @Mapping(target = "name", expression = "java(semester.getName().getSemesterName())")
    AcademicResponseDTO toAcademicResponseDTO(Semester semester);

    @Mapping(target = "name", expression = "java(semester.getName().getSemesterName())")
    @Mapping(target = "yearId", source = "year.id")
    SemesterRequestDTO toSemesterRequestDTO(Semester semester);

    @Mapping(target = "name",
            expression = "java(com.lqm.academic_service.models.SemesterType.fromSemesterName(semesterRequestDTO.name()))")
    @Mapping(target = "year", ignore = true)
    Semester toEntity(SemesterRequestDTO semesterRequestDTO);
}
