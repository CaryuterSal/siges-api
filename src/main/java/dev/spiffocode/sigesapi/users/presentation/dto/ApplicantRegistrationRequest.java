package dev.spiffocode.sigesapi.users.presentation.dto;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder(toBuilder = true)
@Jacksonized
@Value
@NonFinal
@EqualsAndHashCode(callSuper = true)
public class ApplicantRegistrationRequest extends UserRegistrationRequest {
}
