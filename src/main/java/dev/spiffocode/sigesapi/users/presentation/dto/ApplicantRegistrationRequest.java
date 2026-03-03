package dev.spiffocode.sigesapi.users.presentation.dto;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Value
@NonFinal
@EqualsAndHashCode(callSuper = true)
public sealed abstract class ApplicantRegistrationRequest extends UserRegistrationRequest permits StudentRegistrationRequest, InstitutionalStaffRegistrationRequest{
}
