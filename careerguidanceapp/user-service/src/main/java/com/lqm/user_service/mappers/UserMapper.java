package com.lqm.user_service.mappers;

import com.lqm.user_service.dtos.*;
import com.lqm.user_service.models.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "code",
            expression = "java(user.getStudent() != null ? user.getStudent().getCode() : null)")
    UserResponseDTO toUserResponseDTO(User user);

    @Mapping(target = "role", expression = "java(user.getRole().getRoleName())")
    @Mapping(target = "code",
            expression = "java(user.getStudent() != null ? user.getStudent().getCode() : null)")
    UserDetailsResponseDTO toUserDetailsResponseDTO(User user);

    //ignore id only for apis
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().getRoleName() : null)")
    @Mapping(target = "code", expression = "java(user.getStudent() != null ? user.getStudent().getCode() : null)")
    UserRequestDTO toUserRequestDTO(User user);

    @Mapping(target = "student", ignore = true)
    @Mapping(target = "role",
            expression = "java(com.lqm.user_service.models.Role.fromRoleName(userRequestDTO.getRole()))")
    User toEntity(UserRequestDTO userRequestDTO);

}
