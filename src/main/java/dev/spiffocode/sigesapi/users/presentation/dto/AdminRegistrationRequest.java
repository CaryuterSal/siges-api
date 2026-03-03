package dev.spiffocode.sigesapi.users.presentation.dto;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder(toBuilder = true)
@Jacksonized
@Value
@EqualsAndHashCode(callSuper = true)
public class AdminRegistrationRequest extends UserRegistrationRequest {
}
