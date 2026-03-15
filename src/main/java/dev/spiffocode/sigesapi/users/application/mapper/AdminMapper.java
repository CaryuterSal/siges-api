package dev.spiffocode.sigesapi.users.application.mapper;

import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.presentation.dto.AdminRegistrationRequest;
import dev.spiffocode.sigesapi.users.presentation.dto.AdminResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    @Mapping(target = "role", constant = "ADMIN")
    AdminResponse toResponse(Admin admin);

    @Mapping(target = "tokenVersion", constant = "0")
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "notificationPreferences", ignore = true)
    @Mapping(target = "password", source = "rawPassword")
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Admin toEntity(AdminRegistrationRequest adminRegistrationRequest, String rawPassword);
}
