package com.lqm.attendance_service.validators;

import com.lqm.attendance_service.dtos.DeviceRequestDTO;
import com.lqm.attendance_service.services.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class DeviceRequestDTOValidatorTest {

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private DeviceRequestDTOValidator validator;

    private DeviceRequestDTO dto;
    private Errors errors;

    @BeforeEach
    void setUp() {
        UUID classroomId = UUID.randomUUID();
        dto = new DeviceRequestDTO("A1B2C3D4E5F6", classroomId);
        errors = new BeanPropertyBindingResult(dto, "deviceRequestDTO");
    }

    @Test
    void supports_ShouldReturnTrueForDeviceRequestDTO() {
        assertTrue(validator.supports(DeviceRequestDTO.class));
        assertFalse(validator.supports(Object.class));
    }

    @Test
    void getSupportedClass_ShouldReturnDeviceRequestDTO() {
        assertEquals(DeviceRequestDTO.class, validator.getSupportedClass());
    }

    @Test
    void validate_WhenClassroomHasNoDevice_ShouldNotAddError() {
        given(deviceService.existDeviceByClassroomId(dto.classroomId())).willReturn(false);

        validator.validate(dto, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    void validate_WhenClassroomAlreadyHasDevice_ShouldAddError() {
        given(deviceService.existDeviceByClassroomId(dto.classroomId())).willReturn(true);

        validator.validate(dto, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getFieldErrorCount("classroomId"));
        assertEquals("device.classroomId.unique", errors.getFieldError("classroomId").getCode());
    }
}
