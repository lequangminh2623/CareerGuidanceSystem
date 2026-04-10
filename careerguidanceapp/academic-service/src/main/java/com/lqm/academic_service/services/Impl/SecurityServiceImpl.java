package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.UserResponseDTO;
import com.lqm.academic_service.services.SectionService;
import com.lqm.academic_service.services.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("securityService")
@RequiredArgsConstructor
@Slf4j
public class SecurityServiceImpl implements SecurityService {

    private final UserClient userClient;
    private final SectionService sectionService;

    public boolean hasPermission(UUID targetId, String targetType, String permission) {
        if (targetId == null || targetType == null || permission == null) {
            return false;
        }

        try {
            UserResponseDTO currentUser = userClient.getCurrentUser();
            if (currentUser == null) {
                log.warn("Permission check failed: User is not authenticated.");
                return false;
            }

            switch (targetType.toUpperCase()) {
                case "SECTION":
                    return handleSectionPermissions(targetId, currentUser.id(), permission);
                default:
                    log.warn("Permission check failed: Unknown target type '{}'", targetType);
                    return false;
            }
        } catch (Exception e) {
            log.error("Error during permission check for target type {} and permission {}", targetType, permission, e);
            return false;
        }
    }

    private boolean handleSectionPermissions(UUID sectionId, UUID teacherId, String permission) {
        if ("MANAGE_SCORES".equalsIgnoreCase(permission)) {
            return sectionService.existTeacherInSection(teacherId, sectionId);
        }
        log.warn("Permission check failed: Unknown permission '{}' for target type 'SECTION'", permission);
        return false;
    }

}
