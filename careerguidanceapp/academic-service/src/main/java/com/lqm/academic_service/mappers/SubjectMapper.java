package com.lqm.academic_service.mappers;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.SubjectRequestDTO;
import com.lqm.academic_service.models.Subject;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubjectMapper {
    AcademicResponseDTO toAcademicResponseDTO(Subject subject);

    SubjectRequestDTO toSubjectRequestDTO(Subject subject);

    Subject toEntity(SubjectRequestDTO subjectRequestDTO);
}
