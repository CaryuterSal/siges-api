package dev.spiffocode.sigesapi.users.presentation.dto;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder
@Jacksonized
@Value
@NonFinal
@EqualsAndHashCode(callSuper = true)
public sealed class ApplicantResponse extends UserResponse permits StudentResponse, InstitutionalStaffResponse {
    Long lateReturnsCount;
}
