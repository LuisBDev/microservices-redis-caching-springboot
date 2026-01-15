package com.mspoc.users_service.mapper;

import com.mspoc.users_service.dto.request.UserPreferencesRequest;
import com.mspoc.users_service.dto.response.UserPreferencesResponse;
import com.mspoc.users_service.entity.UserPreferences;
import org.mapstruct.*;

/**
 * Mapper para UserPreferences entity usando MapStruct.
 *
 * @author Luis Balarezo
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserPreferencesMapper {

    /**
     * Convierte UserPreferencesRequest a UserPreferences entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserPreferences toEntity(UserPreferencesRequest request);

    /**
     * Convierte UserPreferences entity a UserPreferencesResponse DTO.
     * Incluye campos calculados para ayudar al notification-service.
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "isInQuietHours", expression = "java(preferences.isInQuietHours())")
    @Mapping(target = "canReceiveEmail", expression = "java(preferences.acceptsEmailNotifications())")
    @Mapping(target = "canReceivePush", expression = "java(preferences.acceptsPushNotifications())")
    @Mapping(target = "canReceiveSms", expression = "java(preferences.acceptsSmsNotifications())")
    UserPreferencesResponse toResponse(UserPreferences preferences);

    /**
     * Actualiza un UserPreferences entity existente con datos del request.
     * Solo actualiza los campos no-null del request.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UserPreferencesRequest request, @MappingTarget UserPreferences preferences);
}
