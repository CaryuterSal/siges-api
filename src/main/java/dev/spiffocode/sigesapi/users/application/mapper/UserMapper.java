package dev.spiffocode.sigesapi.users.application.mapper;

import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.InstitutionalStaff;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.model.User;
import dev.spiffocode.sigesapi.users.presentation.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = { AdminMapper.class, InstitutionalStaffMapper.class, StudentMapper.class })
public abstract class UserMapper {

    @Autowired
    protected ReservationRepository reservationRepository;

    public UserResponse toResponse(User entity) {
        return switch (entity) {
            case Student s -> toStudentResponse(s);
            case Admin a -> toAdminResponse(a);
            case InstitutionalStaff i -> toInstitutionalStaffResponse(i);
            default -> throw new IllegalArgumentException("Unknown user type: " + entity.getClass().getSimpleName());
        };
    }

    public List<UserResponse> toResponseList(List<User> entities) {
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
    @Mapping(target = "profilePictureUrl", ignore = true)
    public abstract User updateEntity(@MappingTarget User entity, UserInfoUpdateRequest request);

    @Mapping(target = "role", constant = "STUDENT")
    @Mapping(target = "lateReturnsCount", expression = "java(reservationRepository.countByPetitionerIdAndReturnedLateTrue(entity.getId()))")
    public abstract StudentResponse toStudentResponse(Student entity);

    @Mapping(target = "role", constant = "ADMIN")
    public abstract AdminResponse toAdminResponse(Admin entity);

    @Mapping(target = "role", constant = "INSTITUTIONAL_STAFF")
    @Mapping(target = "lateReturnsCount", expression = "java(reservationRepository.countByPetitionerIdAndReturnedLateTrue(entity.getId()))")
    public abstract InstitutionalStaffResponse toInstitutionalStaffResponse(InstitutionalStaff entity);
}
