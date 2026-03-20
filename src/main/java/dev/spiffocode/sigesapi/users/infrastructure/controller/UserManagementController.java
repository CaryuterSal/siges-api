package dev.spiffocode.sigesapi.users.infrastructure.controller;

import dev.spiffocode.sigesapi.common.presentation.ValidationProblem;
import dev.spiffocode.sigesapi.users.application.service.*;
import dev.spiffocode.sigesapi.users.presentation.dto.*;
import dev.spiffocode.sigesapi.notifications.application.service.PushTokenService;
import dev.spiffocode.sigesapi.auth.infrastructure.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(version = "1.0.0")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for managing and querying users data")
public class UserManagementController {

        private final UserManagementService managementService;
        private final UserQueryService queryService;
        private final PushTokenService pushTokenService;
        private final SecurityContextHelper securityContextHelper;

        @PatchMapping("/users/{id}")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "OK"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "400", description = "Validation problem", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
                        @ApiResponse(responseCode = "409", description = "phone number already in use")
        })
        @Operation(summary = "updates user common data. Available for all admins and the owner of the account")
        public UserResponse updateCommonInfo(
                        @PathVariable Long id,
                        @RequestBody @Valid UserInfoUpdateRequest request) {
                return managementService.updateCommonInfo(id, request);
        }

        @PutMapping("/users/{id}/email")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "OK"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "400", description = "Validation problem", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
                        @ApiResponse(responseCode = "409", description = "email already in use")
        })
        @Operation(summary = "updates the email of any user. Only available for admins")
        public UserResponse updateEmail(
                        @PathVariable Long id,
                        @RequestBody @Valid EmailUpdateRequest request) {
                return managementService.updateEmail(id, request);
        }

        @PutMapping("/students/{id}/registration-number")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "OK"),
                        @ApiResponse(responseCode = "404", description = "Student not found"),
                        @ApiResponse(responseCode = "400", description = "Validation problem", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
                        @ApiResponse(responseCode = "409", description = "student registration number already in use")
        })
        @Operation(summary = "updates the registration number of any student. Only available for admins")
        public StudentResponse updateStudentRegistrationNumber(
                        @PathVariable Long id,
                        @RequestBody @Valid RegNumberUpdateRequest request) {
                return managementService.updateStudentRegistrationNum(id, request);
        }

        @PutMapping("/institutional-staff/{id}/employee-number")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "OK"),
                        @ApiResponse(responseCode = "404", description = "Institutional Staff not found"),
                        @ApiResponse(responseCode = "400", description = "Validation problem", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
                        @ApiResponse(responseCode = "409", description = "employee number already in use")
        })
        @Operation(summary = "updates the employee number of any institutional staff. Only available for admins")
        public InstitutionalStaffResponse updateEmployeeNumber(
                        @PathVariable Long id,
                        @RequestBody @Valid EmpNumberUpdateRequest request) {
                return managementService.updateEmployeeNum(id, request);
        }

        @DeleteMapping("/users/{id}")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Successfully deactivated"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @Operation(summary = "Soft deletes any user. Only available for admins")
        public ResponseEntity<@NonNull Void> deactivateUser(
                        @PathVariable Long id) {
                managementService.deleteUser(id);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/users/{id}/restore")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Successfully restored"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @Operation(summary = "Restores any soft-deleted user. Only available for admins")
        public ResponseEntity<@NonNull Void> restoreUser(
                        @PathVariable Long id) {
                managementService.restoreUser(id);
                return ResponseEntity.noContent().build();
        }

        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true),
                        @ApiResponse(responseCode = "400", description = "Invalid sort field")
        })
        @GetMapping("/users")
        @PageableAsQueryParam
        @Operation(summary = "Search users by filters, order by sorting fields, and page results")
        public Page<@NonNull UserResponse> searchUsers(
                        @ParameterObject @SortDefault("updatedAt") @Schema(description = "Information por paging and sorting by any field") Pageable pageable,
                        @RequestParam(name = "q", required = false) @Schema(description = "Query for searching by email, name, phone number, student registration number or employee number ") String searchQuery,
                        @RequestParam(required = false) @Schema(description = "Filter by the email of the registerer of the account") String createdBy,
                        @RequestParam(defaultValue = "ALL") @Schema(description = "Whether to fetch only ACTIVE records, only DELETED records, or ALL") ShowModeFilter showMode,
                        @RequestParam(required = false) @Schema(description = "Filter by the type of user (admin, student or staff). Can select multiple of them") List<UserTypeFilter> userTypes) {
                UserFilter filter = UserFilter.builder()
                                .searchQuery(searchQuery)
                                .createdBy(createdBy)
                                .showMode(showMode)
                                .userTypes(userTypes)
                                .build();
                return queryService.findAllUsers(pageable, filter);
        }

        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping("/users/{id}")
        @Operation(summary = "Finds a user by its ID. Only available for admins")
        public UserResponse findById(
                        @PathVariable Long id) {
                return queryService.findUserById(id);
        }

        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true),
                @ApiResponse(responseCode = "400", description = "Invalid sort field"),
        })
        @GetMapping("/users/me")
        @Operation(summary = "Finds a user by its ID. Only available for admins")
        public UserResponse findSelfProfile() {
            return queryService.findUserById(securityContextHelper.getCurrentUserId());
        }

        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true),
                        @ApiResponse(responseCode = "400", description = "Invalid sort field"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping("/users/lookup")
        @Operation(description = "Search a user by its unique identifier (email, phone number, employee number, or registration number)")
        public UserResponse lookupByIdentifier(
                        @Schema(description = "Query for searching by its exact email, name, phone number, student registration number or employee number") @RequestParam String identifier) {
                return queryService.findUserByIdentifier(identifier);
        }

        @GetMapping("/users/me/notification-preferences")
        @Operation(summary = "Get the notification preferences for the current user.")
        public List<NotificationPreferenceResponse> getNotificationPreferences() {
                return managementService.getNotificationPreferences(securityContextHelper.getCurrentUserId());
        }

        @PutMapping("/users/me/notification-preferences")
        @Operation(summary = "Update the notification preferences for the current user. Replaces all existing preferences.")
        public List<NotificationPreferenceResponse> updateNotificationPreferences(
                        @RequestBody @Valid List<NotificationPreferenceUpdateRequest> updates) {
                return managementService.updateNotificationPreferences(securityContextHelper.getCurrentUserId(),
                                updates);
        }

        @PostMapping("/users/me/push-tokens")
        @Operation(summary = "Register a new Push Token for the current user.")
        public ResponseEntity<@NonNull Void> registerPushToken(
                        @RequestBody @Valid PushTokenRequest request) {
                pushTokenService.registerToken(securityContextHelper.getCurrentUserId(), request);
                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/users/me/push-tokens/{token}")
        @Operation(summary = "Unregister an existing Push Token for the current user.")
        public ResponseEntity<@NonNull Void> unregisterPushToken(
                        @PathVariable String token) {
                pushTokenService.unregisterToken(securityContextHelper.getCurrentUserId(), token);
                return ResponseEntity.noContent().build();
        }

        @PutMapping(value = "/users/me/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Uploads and updates the profile picture for the current user.")
        public ResponseEntity<@NonNull ProfilePictureResponse> updateProfilePicture(
                        @RequestParam("file") MultipartFile file) {
                String newUrl = managementService.updateProfilePicture(securityContextHelper.getCurrentUserId(), file);
                return ResponseEntity.ok(new ProfilePictureResponse(newUrl));
        }
}
