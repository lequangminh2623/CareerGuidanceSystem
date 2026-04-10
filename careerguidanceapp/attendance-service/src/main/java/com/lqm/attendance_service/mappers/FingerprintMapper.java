package com.lqm.attendance_service.mappers;

import com.lqm.attendance_service.dtos.FingerprintRequestDTO;
import com.lqm.attendance_service.models.Fingerprint;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FingerprintMapper {
    @Mapping(target = "studentName", ignore = true)
    FingerprintRequestDTO toFingerprintRequestDTO(Fingerprint entity);

    Fingerprint toEntity(FingerprintRequestDTO dto);
}
