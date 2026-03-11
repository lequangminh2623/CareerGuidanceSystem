package com.lqm.academic_service.mappers;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.YearRequestDTO;
import com.lqm.academic_service.models.Year;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface YearMapper {
    AcademicResponseDTO toAcademicResponseDTO(Year year);

    YearRequestDTO toYearRequestDTO(Year year);

    Year toEntity(YearRequestDTO yearRequestDTO);
}
