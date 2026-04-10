package com.lqm.attendance_service.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.lqm.attendance_service.dtos.DeviceRequestDTO;
import com.lqm.attendance_service.services.DeviceService;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeviceRequestDTOValidator implements Validator, SupportsClass {

    private final DeviceService deviceService;

    @Override
    public Class<?> getSupportedClass() {
        return DeviceRequestDTO.class;
    }

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return DeviceRequestDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        DeviceRequestDTO deviceRequestDTO = (DeviceRequestDTO) target;

        boolean exists = deviceService.existDeviceByClassroomId(deviceRequestDTO.classroomId());
        if (exists) {
            errors.rejectValue("classroomId", "device.classroomId.unique");
        }

    }
}
