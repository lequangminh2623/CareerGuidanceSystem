package com.lqm.academic_service.mappers;

import com.lqm.academic_service.dtos.CurriculumRequestDTO;
import com.lqm.academic_service.dtos.CurriculumResponseDTO;
import com.lqm.academic_service.models.Curriculum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CurriculumMapper {
    
    @Mapping(target = "semesterName", expression = "java(curriculum.getSemester().getName().getSemesterName())")
    @Mapping(target = "subjectName", source = "subject.name")
    CurriculumResponseDTO toCurriculumResponseDTO(Curriculum curriculum);

    @Mapping(target = "gradeId", source = "grade.id")
    @Mapping(target = "semesterId", source = "semester.id")
    @Mapping(target = "subjectId", source = "subject.id")
    CurriculumRequestDTO toCurriculumRequestDTO(Curriculum curriculum);

    Curriculum toEntity(CurriculumRequestDTO curriculumRequestDTO);
}
