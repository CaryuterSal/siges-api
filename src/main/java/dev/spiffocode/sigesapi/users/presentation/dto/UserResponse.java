package dev.spiffocode.sigesapi.users.presentation.dto;

import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SuperBuilder
@Jacksonized
@Value
@NonFinal
public sealed class UserResponse permits AdminResponse, ApplicantResponse {
    Long id;
    String email;
    String phoneNumber;
    String firstName;
    String lastName;
    LocalDate birthDate;
    LocalDateTime createdAt;
    String createdBy;
    LocalDateTime deletedAt;
    String role;
    Boolean enabled;
    String profilePictureUrl;
}
