package com.lqm.academic_service.mappers;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.YearRequestDTO;
import com.lqm.academic_service.models.Year;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface YearMapper {
    AcademicResponseDTO toAcademicResponseDTO(Year year);

    YearRequestDTO toYearRequestDTO(Year year);

    Year toEntity(YearRequestDTO yearRequestDTO);
}
