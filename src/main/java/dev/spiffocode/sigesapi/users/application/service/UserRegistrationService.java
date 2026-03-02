package dev.spiffocode.sigesapi.users.application.service;

import dev.spiffocode.sigesapi.users.presentation.dto.*;

public interface UserRegistrationService {
    AdminResponse registerAdmin(AdminRegistrationRequest request);
    StudentResponse registerStudent(StudentRegistrationRequest request);
    InstitutionalStaffResponse registerInstitutionalStaff(InstitutionalStaffRegistrationRequest request);
}
