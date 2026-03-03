package dev.spiffocode.sigesapi.users.application.mapper;

import dev.spiffocode.sigesapi.users.domain.model.InstitutionalStaff;
import dev.spiffocode.sigesapi.users.presentation.dto.InstitutionalStaffRegistrationRequest;
import dev.spiffocode.sigesapi.users.presentation.dto.InstitutionalStaffResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InstitutionalStaffMapper {

    @Mapping(target = "role", constant = "INSTITUTIONAL_STAFF")
    InstitutionalStaffResponse toResponse(InstitutionalStaff admin);


    @Mapping(target = "tokenVersion", constant = "0")
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "password", source = "rawPassword")
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    InstitutionalStaff toEntity(InstitutionalStaffRegistrationRequest adminRegistrationRequest, String rawPassword);
}
