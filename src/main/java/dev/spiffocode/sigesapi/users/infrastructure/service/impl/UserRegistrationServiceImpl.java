package dev.spiffocode.sigesapi.users.infrastructure.service.impl;

import dev.spiffocode.sigesapi.users.application.service.PhoneNumberNormalizer;
import dev.spiffocode.sigesapi.users.application.service.UserRegistrationService;
import dev.spiffocode.sigesapi.users.domain.repository.AdminRepository;
import dev.spiffocode.sigesapi.users.domain.repository.InstitutionalStaffRepository;
import dev.spiffocode.sigesapi.users.domain.repository.StudentRepository;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import dev.spiffocode.sigesapi.users.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegistrationServiceImpl implements UserRegistrationService {


    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final InstitutionalStaffRepository institutionalStaffRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhoneNumberNormalizer phoneNumberNormalizer;


    @Override
    public AdminResponse registerAdmin(AdminRegistrationRequest request) {
        return null;
    }

    @Override
    public StudentResponse registerStudent(StudentRegistrationRequest request) {
        return null;
    }

    @Override
    public InstitutionalStaffResponse registerInstitutionalStaff(InstitutionalStaffRegistrationRequest request) {
        return null;
    }
}
