package dev.spiffocode.sigesapi.users.application.mapper;

import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.presentation.dto.StudentRegistrationRequest;
import dev.spiffocode.sigesapi.users.presentation.dto.StudentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "role", constant = "STUDENT")
    StudentResponse toResponse(Student admin);

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
    @Mapping(target = "profilePictureUrl", ignore = true)
    Student toEntity(StudentRegistrationRequest adminRegistrationRequest, String rawPassword);

}
