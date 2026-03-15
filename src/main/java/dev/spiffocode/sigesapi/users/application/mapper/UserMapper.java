package dev.spiffocode.sigesapi.users.application.mapper;

import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.InstitutionalStaff;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.model.User;
import dev.spiffocode.sigesapi.users.presentation.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AdminMapper.class, InstitutionalStaffMapper.class, StudentMapper.class})
public interface UserMapper {
    default UserResponse toResponse(User entity) {
        return switch (entity) {
            case Student s -> toStudentResponse(s);
            case Admin a -> toAdminResponse(a);
            case InstitutionalStaff i -> toInstitutionalStaffResponse(i);
            default -> throw new IllegalArgumentException("Unknown user type: " + entity.getClass());
        };
    }

    default List<UserResponse> toResponseList(List<User> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }

    @Mapping(target = "tokenVersion", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "notificationPreferences", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    User updateEntity(@MappingTarget User entity, UserInfoUpdateRequest request);



    @Mapping(target = "role", constant = "STUDENT")
    StudentResponse toStudentResponse(Student entity);
    @Mapping(target = "role", constant = "ADMIN")
    AdminResponse toAdminResponse(Admin entity);
    @Mapping(target = "role", constant = "INSTITUTIONAL_STAFF")
    InstitutionalStaffResponse toInstitutionalStaffResponse(InstitutionalStaff entity);
}
