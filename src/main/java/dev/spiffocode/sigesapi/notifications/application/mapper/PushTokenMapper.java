package dev.spiffocode.sigesapi.notifications.application.mapper;

import dev.spiffocode.sigesapi.notifications.domain.model.PushToken;
import dev.spiffocode.sigesapi.users.domain.model.User;
import dev.spiffocode.sigesapi.users.presentation.dto.PushTokenRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PushTokenMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUsedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "notificationsSent", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "user", source = "user")
    PushToken toEntity(PushTokenRequest pushTokenRequest, User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUsedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "notificationsSent", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "user", source = "user")
    PushToken updateEntity(PushTokenRequest pushTokenRequest, User user, @MappingTarget PushToken pushToken);
}
