package dev.spiffocode.sigesapi.users.application.service;

import dev.spiffocode.sigesapi.auth.presentation.dto.AuthenticatedResponse;
import dev.spiffocode.sigesapi.users.presentation.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserManagementService {
    UserResponse updateCommonInfo(Long id, UserInfoUpdateRequest request);

    String updateProfilePicture(Long id, MultipartFile file);

    UserResponse updateEmail(Long id, EmailUpdateRequest request);

    StudentResponse updateStudentRegistrationNum(Long id, RegNumberUpdateRequest request);

    InstitutionalStaffResponse updateEmployeeNum(Long id, EmpNumberUpdateRequest request);

    void deleteUser(Long id);

    void restoreUser(Long id);

    List<NotificationPreferenceResponse> getNotificationPreferences(Long userId);

    List<NotificationPreferenceResponse> updateNotificationPreferences(Long userId,
            List<NotificationPreferenceUpdateRequest> updates);

    AuthenticatedResponse updatePassword(PasswordUpdateRequest request, String requestIp);
}
