package dev.spiffocode.sigesapi.users.application.service;

import dev.spiffocode.sigesapi.users.presentation.dto.*;

import java.util.List;

public interface UserManagementService {
    UserResponse updateCommonInfo(Long id, UserInfoUpdateRequest request);

    UserResponse updateEmail(Long id, EmailUpdateRequest request);

    StudentResponse updateStudentRegistrationNum(Long id, RegNumberUpdateRequest request);

    InstitutionalStaffResponse updateEmployeeNum(Long id, EmpNumberUpdateRequest request);

    void deleteUser(Long id);

    void restoreUser(Long id);

    List<NotificationPreferenceResponse> getNotificationPreferences(Long userId);

    List<NotificationPreferenceResponse> updateNotificationPreferences(Long userId,
            List<NotificationPreferenceUpdateRequest> updates);
}
