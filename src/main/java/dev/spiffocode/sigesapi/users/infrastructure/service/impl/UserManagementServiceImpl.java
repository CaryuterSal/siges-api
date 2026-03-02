package dev.spiffocode.sigesapi.users.infrastructure.service.impl;

import dev.spiffocode.sigesapi.users.application.service.PhoneNumberNormalizer;
import dev.spiffocode.sigesapi.users.application.service.UserManagementService;
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
public class UserManagementServiceImpl  implements UserManagementService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final InstitutionalStaffRepository institutionalStaffRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhoneNumberNormalizer phoneNumberNormalizer;

    @Override
    public UserResponse updateCommonInfo(Long id, UserInfoUpdateRequest request) {
        return null;
    }

    @Override
    public UserResponse updateEmail(Long id, EmailUpdateRequest request) {
        return null;
    }

    @Override
    public StudentResponse updateStudentRegistrationNum(Long id, RegNumberUpdateRequest request) {
        return null;
    }

    @Override
    public InstitutionalStaffResponse updateEmployeeNum(Long id, EmpNumberUpdateRequest request) {
        return null;
    }

    @Override
    public void deleteUser(Long id) {

    }

    @Override
    public void restoreUser(Long id) {

    }
}
