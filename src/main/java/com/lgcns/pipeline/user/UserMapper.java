package com.lgcns.pipeline.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roleNames", expression = "java(" +
            "user.getRoles().stream().map(Enum::name).toList())")
    UserDTO toDTO(User user);

    @Mapping(target = "roles", ignore = true)
        // roles 생략
    User toEntity(UserDTO dto);

    @Mapping(target = "id", ignore = true)
        // id 생략
    User toEntity(UserRegistDTO dto);
}
