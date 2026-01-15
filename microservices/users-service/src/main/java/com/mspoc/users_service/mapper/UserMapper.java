package com.mspoc.users_service.mapper;

import com.mspoc.users_service.dto.request.CreateUserRequest;
import com.mspoc.users_service.dto.request.UpdateUserRequest;
import com.mspoc.users_service.dto.response.UserResponse;
import com.mspoc.users_service.entity.User;
import org.mapstruct.*;

/**
 * @author Luis Balarezo
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    /**
     * Convierte CreateUserRequest a User entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "preferences", ignore = true)
    User toEntity(CreateUserRequest request);

    /**
     * Convierte User entity a UserResponse DTO.
     */
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "hasPreferences", expression = "java(user.hasPreferences())")
    UserResponse toResponse(User user);

    /**
     * Actualiza un User entity existente con datos de UpdateUserRequest.
     * Solo actualiza los campos no-null del request.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "preferences", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);
}
