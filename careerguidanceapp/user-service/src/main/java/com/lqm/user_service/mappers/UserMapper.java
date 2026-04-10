package com.lqm.user_service.mappers;

import com.lqm.user_service.dtos.*;
import com.lqm.user_service.models.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "code",
            expression = "java(user.getStudent() != null ? user.getStudent().getCode() : null)")
    UserResponseDTO toUserResponseDTO(User user);

    @Mapping(target = "role", source = "role.roleName")
    UserMessageResponseDTO toUserMessageResponseDTO(User user);

    @Mapping(target = "role", source = "role.roleName")
    @Mapping(target = "code",
            expression = "java(user.getStudent() != null ? user.getStudent().getCode() : null)")
    UserDetailsResponseDTO toUserDetailsResponseDTO(User user);

    //ignore id only for apis
    @Mapping(target = "role", source = "role.roleName")
    @Mapping(target = "code", expression = "java(user.getStudent() != null ? user.getStudent().getCode() : null)")
    AdminUserRequestDTO toAdminUserRequestDTO(User user);

    @Mapping(target = "student", ignore = true)
    @Mapping(target = "role",
            expression = "java(com.lqm.user_service.models.Role.fromRoleName(adminUserRequestDTO.role()))")
    User toEntity(AdminUserRequestDTO adminUserRequestDTO);

    @Mapping(target = "student", ignore = true)
    User toEntity(UserRequestDTO userRequestDTO);

}
