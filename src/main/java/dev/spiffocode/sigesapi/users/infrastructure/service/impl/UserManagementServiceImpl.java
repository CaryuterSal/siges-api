package dev.spiffocode.sigesapi.users.infrastructure.service.impl;

import dev.spiffocode.sigesapi.auth.infrastructure.SecurityContextHelper;
import dev.spiffocode.sigesapi.common.infrastructure.persistence.WithDeletedRecords;
import dev.spiffocode.sigesapi.mailsender.application.service.UserManagementEmailPort;
import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import dev.spiffocode.sigesapi.notifications.domain.model.NotificationPreference;
import dev.spiffocode.sigesapi.users.application.mapper.InstitutionalStaffMapper;
import dev.spiffocode.sigesapi.users.application.mapper.StudentMapper;
import dev.spiffocode.sigesapi.users.application.mapper.UserMapper;
import dev.spiffocode.sigesapi.users.application.service.PhoneNumberNormalizer;
import dev.spiffocode.sigesapi.users.application.service.UserManagementService;
import dev.spiffocode.sigesapi.users.domain.exception.UserNotFoundException;
import dev.spiffocode.sigesapi.users.domain.model.InstitutionalStaff;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.model.User;
import dev.spiffocode.sigesapi.users.domain.repository.AdminRepository;
import dev.spiffocode.sigesapi.users.domain.repository.InstitutionalStaffRepository;
import dev.spiffocode.sigesapi.users.domain.repository.StudentRepository;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import dev.spiffocode.sigesapi.users.presentation.dto.*;
import dev.spiffocode.sigesapi.common.infrastructure.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final InstitutionalStaffRepository institutionalStaffRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhoneNumberNormalizer phoneNumberNormalizer;

    private final UserManagementEmailPort emailPort;

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final InstitutionalStaffMapper institutionalStaffMapper;

    private final UserUniquenessValidator userUniquenessValidator;
    private final StudentUniquenessValidator studentUniquenessValidator;
    private final InstitutionalStaffUniquenessValidator institutionalStaffUniquenessValidator;

    private final SecurityContextHelper securityContextHelper;
    private final StorageService storageService;

    @Override
    public UserResponse updateCommonInfo(Long id, UserInfoUpdateRequest request) {
        if (!securityContextHelper.isAdminOrCurrentUser(id)) {
            throw new AccessDeniedException("You can only update your own information");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        String normalizedPhoneNumber = phoneNumberNormalizer.normalize(request.phoneNumber(), "MX");
        userUniquenessValidator.assertCommonInfoUpdateUnique(id, normalizedPhoneNumber);
        user = userMapper.updateEntity(user, request);

        boolean updateTokenVersion = !securityContextHelper.isCurrentUser(user.getId());
        user.changePhoneNumber(normalizedPhoneNumber, updateTokenVersion);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public String updateProfilePicture(Long id, MultipartFile file) {
        if (!securityContextHelper.isCurrentUser(id)) {
            throw new AccessDeniedException("You can only update your own profile picture");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        String oldUrl = user.getProfilePictureUrl();
        if (oldUrl != null && !oldUrl.isBlank()) {
            try {
                storageService.deleteFile(oldUrl);
            } catch (Exception e) {
                log.warn("Could not delete old profile picture", e);
            }
        }

        String newUrl = storageService.uploadFile( file, "avatars");
        user.setProfilePictureUrl(newUrl);
        userRepository.save(user);

        return newUrl;
    }

    @Override
    public UserResponse updateEmail(Long id, EmailUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userUniquenessValidator.assertEmailChangeUnique(id, request.email());
        String oldEmail = user.getEmail();

        boolean updateTokenVersion = !securityContextHelper.isCurrentUser(user.getId());
        user.changeEmail(request.email(), updateTokenVersion);
        userRepository.save(user);

        emailPort.sendEmailChangeEmail(oldEmail, request.email(), user.fullName());
        return userMapper.toResponse(user);
    }

    @Override
    public StudentResponse updateStudentRegistrationNum(Long id, RegNumberUpdateRequest request) {
        Student user = studentRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        studentUniquenessValidator.assertUpdateUnique(id, request.registrationNumber());
        String oldRegNumber = user.getRegistrationNumber();

        boolean updateTokenVersion = !securityContextHelper.isAdminOrCurrentUser(user.getId());
        user.changeRegistrationNumber(request.registrationNumber(), updateTokenVersion);
        userRepository.save(user);

        emailPort.sendStudentRegistrationNumberChangeEmail(user.getEmail(), user.fullName(), oldRegNumber,
                request.registrationNumber());
        return studentMapper.toResponse(user);
    }

    @Override
    public InstitutionalStaffResponse updateEmployeeNum(Long id, EmpNumberUpdateRequest request) {
        InstitutionalStaff user = institutionalStaffRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        institutionalStaffUniquenessValidator.assertUpdateUnique(id, request.employeeNumber());
        String oldEmpNum = user.getEmployeeNumber();

        boolean updateTokenVersion = !securityContextHelper.isAdminOrCurrentUser(user.getId());
        user.changeEmployeeNumber(request.employeeNumber(), updateTokenVersion);
        userRepository.save(user);

        emailPort.sendEmployeeNumberChangeEmail(user.getEmail(), user.fullName(), oldEmpNum, request.employeeNumber());
        return institutionalStaffMapper.toResponse(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.softDeleteById(id);

        emailPort.sendGoodbyeEmail(user.getEmail(), user.fullName());
        // TODO: Cancel all reservations
    }

    @WithDeletedRecords
    @Override
    public void restoreUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.restoreById(id);
        emailPort.sendAccountRestoredEmail(user.getEmail(), user.fullName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> getNotificationPreferences(Long userId) {
        if (!securityContextHelper.isAdminOrCurrentUser(userId)) {
            throw new AccessDeniedException("You can only view your own notification preferences");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Map<Type, NotificationPreference> existingPrefs = user.getNotificationPreferences().stream()
                .collect(Collectors.toMap(NotificationPreference::getType, Function.identity()));

        return java.util.Arrays.stream(Type.values())
                .map(type -> {
                    NotificationPreference pref = existingPrefs.get(type);
                    if (pref == null) {
                        return new NotificationPreferenceResponse(type, true, true);
                    }
                    return new NotificationPreferenceResponse(type, pref.isEmailEnabled(), pref.isInAppEnabled());
                })
                .toList();
    }

    @Override
    public List<NotificationPreferenceResponse> updateNotificationPreferences(Long userId,
            List<NotificationPreferenceUpdateRequest> updates) {
        if (!securityContextHelper.isAdminOrCurrentUser(userId)) {
            throw new AccessDeniedException("You can only update your own notification preferences");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Map<Type, NotificationPreference> existingPrefs = user.getNotificationPreferences().stream()
                .collect(Collectors.toMap(NotificationPreference::getType, Function.identity()));

        for (NotificationPreferenceUpdateRequest update : updates) {
            NotificationPreference pref = existingPrefs.get(update.type());
            if (pref == null) {
                pref = NotificationPreference.builder()
                        .user(user)
                        .type(update.type())
                        .emailEnabled(update.emailEnabled())
                        .inAppEnabled(update.inAppEnabled())
                        .build();
                user.getNotificationPreferences().add(pref);
                existingPrefs.put(update.type(), pref);
            } else {
                pref.setEmailEnabled(update.emailEnabled());
                pref.setInAppEnabled(update.inAppEnabled());
            }
        }

        userRepository.save(user);

        return getNotificationPreferences(userId);
    }
}
