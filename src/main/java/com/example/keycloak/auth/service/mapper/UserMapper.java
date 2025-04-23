package com.example.keycloak.auth.service.mapper;

import com.example.keycloak.auth.service.model.dto.RegisterRequest;
import com.example.keycloak.auth.service.model.dto.UserResponse;
import com.example.keycloak.auth.service.model.entity.UserEntity;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "enabled", expression = "java(true)")
    @Mapping(target = "emailVerified", expression = "java(false)")
    UserRepresentation toUserRepresentation(RegisterRequest request);

    @Mapping(target = "firstName", source = "request.firstName")
    @Mapping(target = "lastName", source = "request.lastName")
    @Mapping(target = "email", source = "request.email")
    @Mapping(target = "enabled", expression = "java(representation.isEnabled())")
    UserEntity toUserEntity(RegisterRequest request, UserRepresentation representation);

    UserResponse toUserResponse(UserEntity userEntity);
}
