package dev.spiffocode.sigesapi.users.infrastructure.service.impl;

import dev.spiffocode.sigesapi.mailsender.application.service.UserManagementEmailPort;
import dev.spiffocode.sigesapi.users.application.mapper.AdminMapper;
import dev.spiffocode.sigesapi.users.application.mapper.InstitutionalStaffMapper;
import dev.spiffocode.sigesapi.users.application.mapper.StudentMapper;
import dev.spiffocode.sigesapi.users.application.mapper.UserMapper;
import dev.spiffocode.sigesapi.users.application.service.PasswordGenerator;
import dev.spiffocode.sigesapi.users.application.service.PhoneNumberNormalizer;
import dev.spiffocode.sigesapi.users.application.service.UserRegistrationService;
import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.InstitutionalStaff;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.AdminRepository;
import dev.spiffocode.sigesapi.users.domain.repository.InstitutionalStaffRepository;
import dev.spiffocode.sigesapi.users.domain.repository.StudentRepository;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import dev.spiffocode.sigesapi.users.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserRegistrationServiceImpl implements UserRegistrationService {


    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final InstitutionalStaffRepository institutionalStaffRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhoneNumberNormalizer phoneNumberNormalizer;
    private final PasswordGenerator passwordGenerator;

    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final StudentMapper studentMapper;
    private final InstitutionalStaffMapper institutionalStaffMapper;

    private final UserUniquenessValidator userUniquenessValidator;
    private final StudentUniquenessValidator studentUniquenessValidator;
    private final InstitutionalStaffUniquenessValidator institutionalStaffUniquenessValidator;

    private final UserManagementEmailPort emailSender;


    @Override
    public AdminResponse registerAdmin(AdminRegistrationRequest request) {
        String normalizedPhoneNumber = phoneNumberNormalizer.normalize(request.getPhoneNumber(), "MX");

        userUniquenessValidator.assertRegisterUnique(request.getEmail(), normalizedPhoneNumber);

        String rawPassword = passwordGenerator.generatePassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Admin entity = adminMapper.toEntity(request, encodedPassword);

        entity.changePhoneNumber(normalizedPhoneNumber);
        entity = adminRepository.save(entity);

        emailSender.sendAdminWelcomeEmail(request.getEmail(), entity.fullName(), rawPassword);

        return adminMapper.toResponse(entity);
    }

    @Override
    public StudentResponse registerStudent(StudentRegistrationRequest request) {

        String normalizedPhoneNumber = phoneNumberNormalizer.normalize(request.getPhoneNumber(), "MX");

        userUniquenessValidator.assertRegisterUnique(request.getEmail(), normalizedPhoneNumber);
        studentUniquenessValidator.assertRegisterUnique(request.getRegistrationNumber());

        String rawPassword = passwordGenerator.generatePassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Student entity = studentMapper.toEntity(request, encodedPassword);
        entity.changePhoneNumber(normalizedPhoneNumber);
        entity = studentRepository.save(entity);

        emailSender.sendStudentWelcomeEmail(request.getEmail(), entity.fullName(), rawPassword);

        return studentMapper.toResponse(entity);
    }

    @Override
    public InstitutionalStaffResponse registerInstitutionalStaff(InstitutionalStaffRegistrationRequest request) {

        String normalizedPhoneNumber = phoneNumberNormalizer.normalize(request.getPhoneNumber(), "MX");

        userUniquenessValidator.assertRegisterUnique(request.getEmail(), normalizedPhoneNumber);
        institutionalStaffUniquenessValidator.assertRegisterUnique(request.getEmployeeNumber());


        String rawPassword = passwordGenerator.generatePassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        InstitutionalStaff entity = institutionalStaffMapper.toEntity(request, encodedPassword);

        entity.changePhoneNumber(normalizedPhoneNumber);
        entity = institutionalStaffRepository.save(entity);

        emailSender.sendInstitutionalStaffWelcomeEmail(request.getEmail(), entity.fullName(), rawPassword);

        return institutionalStaffMapper.toResponse(entity);
    }
}
