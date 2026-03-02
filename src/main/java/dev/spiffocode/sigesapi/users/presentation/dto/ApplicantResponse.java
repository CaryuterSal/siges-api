package dev.spiffocode.sigesapi.users.presentation.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@Value
@NonFinal
@EqualsAndHashCode(callSuper = true)
public sealed class ApplicantResponse extends UserResponse permits StudentResponse, InstitutionalStaffResponse{
    String registrationNumber;
}
